package top.mddata.base.idempotent.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.mddata.base.cache.repository.CachePlusOps;
import top.mddata.base.idempotent.properties.IdempotencyProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisIdempotencyRepositoryTest {
    @Mock
    private CachePlusOps cachePlusOps;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RedisIdempotencyRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RedisIdempotencyRepository(
                cachePlusOps, objectMapper, new IdempotencyProperties());
    }

    @Test
    void issueUsesNxAndUnusedTtl() throws JsonProcessingException {
        when(cachePlusOps.eval(anyString(), anyList(), anyList(), eq(Long.class)))
                .thenReturn(1L);

        repository.issue("test-key", "user:7");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> args = ArgumentCaptor.forClass(List.class);
        verify(cachePlusOps).eval(contains("NX"), eq(List.of("mdp:idempotent:test-key")),
                args.capture(), eq(Long.class));
        assertEquals("600000", args.getValue().get(1));
        assertEquals("UNUSED", objectMapper.readTree(args.getValue().get(0)).get("state").asText());
    }

    @Test
    void acquireRestoresFirstResponse() {
        when(cachePlusOps.eval(anyString(), anyList(), anyList(), eq(String.class)))
                .thenReturn("{\"outcome\":\"REPLAY\",\"response\":{"
                        + "\"status\":200,\"contentType\":\"application/json\","
                        + "\"body\":\"{\\\"data\\\":101}\"}}");

        IdempotencyRepository.Acquisition result = repository.acquire(
                "test-key", "user:7", "digest");

        assertEquals(IdempotencyRepository.Outcome.REPLAY, result.outcome());
        assertEquals("{\"data\":101}", result.response().body());
    }

    @Test
    void staleExecutionCannotComplete() {
        when(cachePlusOps.eval(anyString(), anyList(), anyList(), eq(Long.class)))
                .thenReturn(0L);

        assertThrows(IllegalStateException.class, () -> repository.complete(
                "test-key", "old-execution",
                new IdempotencyRepository.CachedResponse(200, "application/json", "{}")));
    }

    @Test
    void acquireScriptGuardsStateSubjectAndFingerprint() {
        when(cachePlusOps.eval(anyString(), anyList(), anyList(), eq(String.class)))
                .thenReturn("{\"outcome\":\"INVALID\"}");

        repository.acquire("test-key", "user:7", "digest");

        ArgumentCaptor<String> script = ArgumentCaptor.forClass(String.class);
        verify(cachePlusOps).eval(script.capture(), anyList(), anyList(), eq(String.class));
        String lua = script.getValue();
        assertTrue(lua.contains("'PROCESSING'"));
        assertTrue(lua.contains("'SUCCESS'"));
        assertTrue(lua.contains("record.subject ~= ARGV[1]"));
        assertTrue(lua.contains("record.fingerprint ~= ARGV[2]"));
    }

    @Test
    void releaseRenewsUnusedTtlAndRejectsStaleExecution() {
        when(cachePlusOps.eval(anyString(), anyList(), anyList(), eq(Long.class)))
                .thenReturn(1L, 0L);

        boolean released = repository.release("test-key", "current-execution");
        boolean staleRejected = repository.release("test-key", "stale-execution");

        assertTrue(released);
        assertFalse(staleRejected);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> args = ArgumentCaptor.forClass(List.class);
        verify(cachePlusOps, times(2)).eval(anyString(),
                eq(List.of("mdp:idempotent:test-key")), args.capture(), eq(Long.class));
        assertEquals("current-execution", args.getAllValues().get(0).get(0));
        assertEquals("600000", args.getAllValues().get(0).get(1));
        assertEquals("stale-execution", args.getAllValues().get(1).get(0));
        assertEquals("600000", args.getAllValues().get(1).get(1));
    }

    @Test
    void issueStateJsonUsesFixedFieldNamesRegardlessOfInjectedMapper() throws JsonProcessingException {
        ObjectMapper snakeMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        RedisIdempotencyRepository snakeRepository = new RedisIdempotencyRepository(
                cachePlusOps, snakeMapper, new IdempotencyProperties());
        when(cachePlusOps.eval(anyString(), anyList(), anyList(), eq(Long.class)))
                .thenReturn(1L);

        snakeRepository.issue("test-key", "user:7");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> args = ArgumentCaptor.forClass(List.class);
        verify(cachePlusOps).eval(anyString(), anyList(), args.capture(), eq(Long.class));
        JsonNode stateNode = snakeMapper.readTree(args.getValue().get(0));
        assertEquals("UNUSED", stateNode.get("state").asText());
        assertEquals("user:7", stateNode.get("subject").asText());
        assertTrue(stateNode.has("fingerprint"));
        assertTrue(stateNode.has("executionId"));
        assertTrue(stateNode.has("status"));
        assertTrue(stateNode.has("contentType"));
        assertTrue(stateNode.has("responseBody"));
        assertFalse(stateNode.has("execution_id"));
        assertFalse(stateNode.has("content_type"));
        assertFalse(stateNode.has("response_body"));
    }

    @Test
    void acquireReplayJsonUsesFixedFieldNamesRegardlessOfInjectedMapper() {
        ObjectMapper snakeMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        RedisIdempotencyRepository snakeRepository = new RedisIdempotencyRepository(
                cachePlusOps, snakeMapper, new IdempotencyProperties());
        when(cachePlusOps.eval(anyString(), anyList(), anyList(), eq(String.class)))
                .thenReturn("{\"outcome\":\"REPLAY\",\"executionId\":\"exec-1\","
                        + "\"response\":{\"status\":200,"
                        + "\"contentType\":\"application/json\","
                        + "\"body\":\"{\\\"data\\\":101}\"}}");

        IdempotencyRepository.Acquisition result = snakeRepository.acquire(
                "test-key", "user:7", "digest");

        assertEquals(IdempotencyRepository.Outcome.REPLAY, result.outcome());
        assertEquals("exec-1", result.executionId());
        assertNotNull(result.response());
        assertEquals(200, result.response().status());
        assertEquals("application/json", result.response().contentType());
        assertEquals("{\"data\":101}", result.response().body());
    }
}
