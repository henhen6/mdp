package top.mddata.base.idempotent.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;
import top.mddata.base.base.R;
import top.mddata.base.idempotent.repository.IdempotencyRepository;
import top.mddata.base.idempotent.repository.IdempotencyRepository.Execution;
import top.mddata.base.idempotent.support.IdempotencyConstants;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 真实 DispatcherServlet 异常处理链回归：首次成功响应处理失败后，全局异常响应不得释放令牌。
 */
class IdempotencyResponseAdviceMvcTest {

    private IdempotencyRepository repository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        repository = mock(IdempotencyRepository.class);
        IdempotencyResponseAdvice advice = new IdempotencyResponseAdvice(repository, new ObjectMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(new FixtureController())
                .setControllerAdvice(advice, new FixtureExceptionHandler())
                .addInterceptors(new ExecutionInterceptor())
                .build();
    }

    @Test
    void completeFailureThenGlobalErrorResponseNeverReleases() throws Exception {
        doThrow(new IllegalStateException("缓存写入失败")).when(repository)
                .complete(anyString(), anyString(), any());

        mockMvc.perform(get("/fixture/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(-10));

        verify(repository, never()).release(anyString(), anyString());
    }

    @Test
    void serializationFailureThenGlobalErrorResponseNeverReleases() throws Exception {
        mockMvc.perform(get("/fixture/serialization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(-10));

        verify(repository, never()).release(anyString(), anyString());
    }

    private static class ExecutionInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            request.setAttribute(IdempotencyConstants.EXECUTION_ATTRIBUTE,
                    new Execution("mvc-test-token", "mvc-execution"));
            return true;
        }
    }

    @RestController
    private static class FixtureController {
        @GetMapping("/fixture/complete")
        R<Long> complete() {
            return R.success(1001L);
        }

        @GetMapping("/fixture/serialization")
        R<Map<String, Object>> serialization() {
            Map<String, Object> cyclic = new HashMap<>();
            cyclic.put("self", cyclic);
            return R.success(cyclic);
        }
    }

    @ControllerAdvice
    private static class FixtureExceptionHandler {
        @ExceptionHandler(RuntimeException.class)
        @ResponseBody
        R<Void> handle(RuntimeException exception) {
            return R.fail(-10, "响应处理失败");
        }
    }
}
