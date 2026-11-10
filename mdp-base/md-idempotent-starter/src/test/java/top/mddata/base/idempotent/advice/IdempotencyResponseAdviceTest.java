package top.mddata.base.idempotent.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import top.mddata.base.base.R;
import top.mddata.base.idempotent.repository.IdempotencyRepository;
import top.mddata.base.idempotent.repository.IdempotencyRepository.CachedResponse;
import top.mddata.base.idempotent.repository.IdempotencyRepository.Execution;
import top.mddata.base.idempotent.support.IdempotencyConstants;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link IdempotencyResponseAdvice} 的行为测试：成功缓存、业务失败释放、不确定结果保留。
 *
 * @author henhen6
 */
@ExtendWith(MockitoExtension.class)
class IdempotencyResponseAdviceTest {

    @Mock
    private IdempotencyRepository repository;

    private ObjectMapper objectMapper;
    private IdempotencyResponseAdvice advice;
    private ServletServerHttpRequest serverRequest;
    private ServletServerHttpResponse serverResponse;
    private MethodParameter returnType;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        repository = mock(IdempotencyRepository.class);
        objectMapper = new ObjectMapper();
        advice = new IdempotencyResponseAdvice(repository, objectMapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(IdempotencyConstants.EXECUTION_ATTRIBUTE,
                new Execution("token-1", "execution-1"));
        serverRequest = new ServletServerHttpRequest(request);
        serverResponse = new ServletServerHttpResponse(new MockHttpServletResponse());
        returnType = new MethodParameter(Fixture.class.getDeclaredMethod("save"), -1);
    }

    private static class Fixture {
        R<Long> save() {
            return null;
        }
    }

    @Test
    void successCachesFinalResponse() throws Exception {
        R<Long> body = R.success(1001L);

        Object returned = advice.beforeBodyWrite(body, returnType,
                MediaType.APPLICATION_JSON, MappingJackson2HttpMessageConverter.class,
                serverRequest, serverResponse);

        assertSame(body, returned);
        ArgumentCaptor<CachedResponse> captor = ArgumentCaptor.forClass(CachedResponse.class);
        verify(repository).complete(eq("token-1"), eq("execution-1"), captor.capture());
        CachedResponse cached = captor.getValue();
        assertEquals(200, cached.status());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, cached.contentType());
        assertEquals(1001, objectMapper.readTree(cached.body()).get("data").asLong());
    }

    @Test
    void businessFailureReleasesToken() {
        R<Object> body = R.fail(-9, "用户名已存在");
        Object returned = advice.beforeBodyWrite(body, returnType, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, serverRequest, serverResponse);

        assertSame(body, returned);
        assertEquals(-9, returned instanceof R<?> r ? r.getCode() : -1);
        verify(repository).release("token-1", "execution-1");
        verify(repository, never()).complete(anyString(), anyString(), any());
    }

    @Test
    void unknownBodyNeverReleasesToken() {
        assertThrows(IllegalStateException.class, () -> advice.beforeBodyWrite(
                "not-R", returnType, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, serverRequest, serverResponse));
        verify(repository, never()).release(anyString(), anyString());
        verify(repository, never()).complete(anyString(), anyString(), any());
    }

    @Test
    void serializationFailureKeepsProcessingWithoutRelease() {
        Map<String, Object> cyclic = new HashMap<>();
        cyclic.put("self", cyclic);
        R<Map<String, Object>> body = R.success(cyclic);

        assertThrows(RuntimeException.class, () -> advice.beforeBodyWrite(body, returnType,
                MediaType.APPLICATION_JSON, MappingJackson2HttpMessageConverter.class,
                serverRequest, serverResponse));
        verify(repository, never()).release(anyString(), anyString());
        verify(repository, never()).complete(anyString(), anyString(), any());
    }

    @Test
    void completeFailureKeepsProcessingWithoutRelease() {
        doThrow(new IllegalStateException("缓存写入失败")).when(repository)
                .complete(anyString(), anyString(), any());
        R<Long> body = R.success(1001L);

        assertThrows(IllegalStateException.class, () -> advice.beforeBodyWrite(body, returnType,
                MediaType.APPLICATION_JSON, MappingJackson2HttpMessageConverter.class,
                serverRequest, serverResponse));
        verify(repository, never()).release(anyString(), anyString());
    }

    @Test
    void releaseFailureAppendsMessageAndKeepsBusinessResponse() {
        doThrow(new IllegalStateException("释放异常")).when(repository)
                .release(anyString(), anyString());
        R<Object> body = R.fail(-9, "用户名已存在");

        Object returned = advice.beforeBodyWrite(body, returnType, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, serverRequest, serverResponse);

        assertSame(body, returned);
        assertEquals(-9, body.getCode());
        assertEquals("用户名已存在；幂等状态释放失败", body.getMsg());
        verify(repository, never()).complete(anyString(), anyString(), any());
    }

    @Test
    void plainRequestWithoutExecutionHasNoRepositoryInteraction() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletServerHttpRequest plainRequest = new ServletServerHttpRequest(request);
        Object body = "plain-body";

        Object returned = advice.beforeBodyWrite(body, returnType, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class, plainRequest, serverResponse);

        assertSame(body, returned);
        verifyNoInteractions(repository);
    }

    @Test
    void adviceIsLowestPrecedenceControllerAdvice() {
        Order order = IdempotencyResponseAdvice.class.getAnnotation(Order.class);
        assertNotNull(order);
        assertEquals(Ordered.LOWEST_PRECEDENCE, order.value());
        assertNotNull(IdempotencyResponseAdvice.class.getAnnotation(ControllerAdvice.class));
    }
}
