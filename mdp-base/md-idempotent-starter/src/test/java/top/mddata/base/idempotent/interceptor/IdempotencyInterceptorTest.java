package top.mddata.base.idempotent.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import top.mddata.base.annotation.idempotent.DuplicateStrategy;
import top.mddata.base.annotation.idempotent.Idempotent;
import top.mddata.base.idempotent.filter.CachedBodyRequestWrapper;
import top.mddata.base.idempotent.repository.IdempotencyRepository;
import top.mddata.base.idempotent.repository.IdempotencyRepository.Acquisition;
import top.mddata.base.idempotent.repository.IdempotencyRepository.Execution;
import top.mddata.base.idempotent.repository.IdempotencyRepository.Outcome;
import top.mddata.base.idempotent.support.IdempotencyConstants;
import top.mddata.base.idempotent.support.IdempotencyResponseWriter;
import top.mddata.base.util.ContextUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link IdempotencyInterceptor} 的行为测试：注解识别、状态分发、首次响应重放与并发抢占。
 *
 * @author henhen6
 */
@ExtendWith(MockitoExtension.class)
class IdempotencyInterceptorTest {

    private static final int DUPLICATE_CODE = -14;
    private static final int CONFLICT_CODE = -15;
    private static final int INVALID_CODE = -16;
    private static final int BODY_UNSUPPORTED_CODE = -6;

    @Mock
    private IdempotencyRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private IdempotencyInterceptor interceptor;
    private HandlerMethod idempotentHandler;
    private HandlerMethod rejectDuplicateHandler;
    private HandlerMethod plainHandler;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        interceptor = new IdempotencyInterceptor(repository, new IdempotencyResponseWriter(objectMapper));
        idempotentHandler = new HandlerMethod(new Fixture(), Fixture.class.getDeclaredMethod("idempotent"));
        rejectDuplicateHandler = new HandlerMethod(
                new Fixture(), Fixture.class.getDeclaredMethod("rejectDuplicate"));
        plainHandler = new HandlerMethod(new Fixture(), Fixture.class.getDeclaredMethod("plain"));
    }

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    void noAnnotationPassesWithoutRepository() throws Exception {
        ContextUtil.setUserId(7L);
        MockHttpServletRequest request = jsonRequest("token-1", "{}");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), plainHandler));
        verifyNoInteractions(repository);
    }

    @Test
    void nonHandlerMethodPassesWithoutRepository() throws Exception {
        ContextUtil.setUserId(7L);
        MockHttpServletRequest request = jsonRequest("token-1", "{}");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        verifyNoInteractions(repository);
    }

    @Test
    void acquiredRequestStoresExecution() throws Exception {
        ContextUtil.setUserId(7L);
        when(repository.acquire(anyString(), eq("user:7"), anyString()))
                .thenReturn(new Acquisition(Outcome.ACQUIRED, "execution-1", null));
        CachedBodyRequestWrapper request = wrappedRequest("token-1", "{}");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), idempotentHandler));
        Execution execution = (Execution) request.getAttribute(IdempotencyConstants.EXECUTION_ATTRIBUTE);
        assertEquals("execution-1", execution.executionId());
        assertEquals("token-1", execution.token());
    }

    @Test
    void 双层包装时沿包装链找到缓存体() throws Exception {
        // 模拟幂等 Filter 之后又有其他包装型 Filter（如 XSS 清洗）再包一层：
        // 最外层不是 CachedBodyRequestWrapper，须沿包装链向下查找
        ContextUtil.setUserId(7L);
        when(repository.acquire(anyString(), eq("user:7"), anyString()))
                .thenReturn(new Acquisition(Outcome.ACQUIRED, "execution-1", null));
        CachedBodyRequestWrapper inner = wrappedRequest("token-1", "{}");
        jakarta.servlet.http.HttpServletRequest outer =
                new jakarta.servlet.http.HttpServletRequestWrapper(inner);

        assertTrue(interceptor.preHandle(outer, new MockHttpServletResponse(), idempotentHandler));
        Execution execution = (Execution) outer.getAttribute(IdempotencyConstants.EXECUTION_ATTRIBUTE);
        assertEquals("execution-1", execution.executionId());
    }

    @Test
    void processingDoesNotEnterController() throws Exception {
        ContextUtil.setUserId(7L);
        when(repository.acquire(anyString(), anyString(), anyString()))
                .thenReturn(new Acquisition(Outcome.PROCESSING, null, null));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(wrappedRequest("token-1", "{}"), response, idempotentHandler));
        assertEquals(DUPLICATE_CODE, responseCodeOf(response).get("code").asInt());
    }

    @Test
    void successReplayWritesFirstBody() throws Exception {
        ContextUtil.setUserId(7L);
        when(repository.acquire(anyString(), anyString(), anyString()))
                .thenReturn(new Acquisition(Outcome.REPLAY, null,
                        new IdempotencyRepository.CachedResponse(
                                200, MediaType.APPLICATION_JSON_VALUE, "{\"code\":0,\"data\":1001}")));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(wrappedRequest("token-1", "{}"), response, idempotentHandler));
        assertEquals(200, response.getStatus());
        assertEquals(1001, responseCodeOf(response).get("data").asLong());
    }

    @Test
    void successReplayRejectsWhenConfigured() throws Exception {
        ContextUtil.setUserId(7L);
        when(repository.acquire(anyString(), anyString(), anyString()))
                .thenReturn(new Acquisition(Outcome.REPLAY, null,
                        new IdempotencyRepository.CachedResponse(
                                200, MediaType.APPLICATION_JSON_VALUE, "{\"code\":0,\"data\":1001}")));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(
                wrappedRequest("token-1", "{}"), response, rejectDuplicateHandler));
        assertEquals(DUPLICATE_CODE, responseCodeOf(response).get("code").asInt());
        assertEquals("请求已处理，请勿重复提交", responseCodeOf(response).get("msg").asText());
    }

    @ParameterizedTest
    @EnumSource(value = Outcome.class, names = {"PROCESSING", "CONFLICT", "INVALID"})
    void nonReplayOutcomesIgnoreDuplicateStrategy(Outcome outcome) throws Exception {
        ContextUtil.setUserId(7L);
        when(repository.acquire(anyString(), anyString(), anyString()))
                .thenReturn(new Acquisition(outcome, null, null));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(
                wrappedRequest("token-1", "{}"), response, rejectDuplicateHandler));
        assertEquals(expectedCode(outcome), responseCodeOf(response).get("code").asInt());
    }

    @ParameterizedTest
    @EnumSource(value = Outcome.class, names = {"CONFLICT", "INVALID"})
    void rejectedOutcomesWriteFailResponseWithoutEnteringController(Outcome outcome) throws Exception {
        ContextUtil.setUserId(7L);
        when(repository.acquire(anyString(), anyString(), anyString()))
                .thenReturn(new Acquisition(outcome, null, null));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(wrappedRequest("token-1", "{}"), response, idempotentHandler));
        assertEquals(expectedCode(outcome), responseCodeOf(response).get("code").asInt());
    }

    @Test
    void missingHeaderWritesInvalidWithoutRepository() throws Exception {
        ContextUtil.setUserId(7L);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, idempotentHandler));
        assertEquals(INVALID_CODE, responseCodeOf(response).get("code").asInt());
        verifyNoInteractions(repository);
    }

    @Test
    void anonymousIdentityWritesInvalidWithoutRepository() throws Exception {
        CachedBodyRequestWrapper request = wrappedRequest("token-1", "{}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, idempotentHandler));
        assertEquals(INVALID_CODE, responseCodeOf(response).get("code").asInt());
        verifyNoInteractions(repository);
    }

    @Test
    void nonWrappedRequestWritesBodyUnsupportedWithoutRepository() throws Exception {
        ContextUtil.setUserId(7L);
        MockHttpServletRequest request = jsonRequest("token-1", "{}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, idempotentHandler));
        assertEquals(BODY_UNSUPPORTED_CODE, responseCodeOf(response).get("code").asInt());
        verifyNoInteractions(repository);
    }

    @Test
    void concurrentRequestsLetOnlyOneEnterController() throws Exception {
        ContextUtil.setUserId(7L);
        AtomicBoolean winner = new AtomicBoolean(false);
        when(repository.acquire(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            if (winner.compareAndSet(false, true)) {
                return new Acquisition(Outcome.ACQUIRED, "execution-winner", null);
            }
            return new Acquisition(Outcome.PROCESSING, null, null);
        });

        int threads = 12;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit((Callable<Boolean>) () -> {
                ContextUtil.setUserId(7L);
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    boolean entered = interceptor.preHandle(
                            wrappedRequest("token-1", "{}"), response, idempotentHandler);
                    if (!entered) {
                        assertEquals(DUPLICATE_CODE, responseCodeOf(response).get("code").asInt());
                    }
                    return entered;
                } finally {
                    ContextUtil.remove();
                }
            }));
        }
        long enteredCount = 0;
        try {
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    enteredCount++;
                }
            }
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, enteredCount);
    }

    private static int expectedCode(Outcome outcome) {
        return switch (outcome) {
            case PROCESSING -> DUPLICATE_CODE;
            case CONFLICT -> CONFLICT_CODE;
            case INVALID -> INVALID_CODE;
            default -> throw new IllegalArgumentException("非预期的拒绝结果: " + outcome);
        };
    }

    private JsonNode responseCodeOf(MockHttpServletResponse response) throws IOException {
        return objectMapper.readTree(response.getContentAsString());
    }

    private CachedBodyRequestWrapper wrappedRequest(String token, String body) throws IOException {
        MockHttpServletRequest request = jsonRequest(token, body);
        return new CachedBodyRequestWrapper(request, 1024);
    }

    private MockHttpServletRequest jsonRequest(String token, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.addHeader(IdempotencyConstants.HEADER, token);
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }

    private static class Fixture {
        @Idempotent
        public void idempotent() {
        }

        @Idempotent(onDuplicate = DuplicateStrategy.REJECT)
        public void rejectDuplicate() {
        }

        public void plain() {
        }
    }
}
