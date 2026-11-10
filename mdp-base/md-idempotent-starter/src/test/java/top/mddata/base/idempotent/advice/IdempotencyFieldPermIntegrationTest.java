package top.mddata.base.idempotent.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.idempotent.Idempotent;
import top.mddata.base.apiperm.model.UserApiPerm;
import top.mddata.base.apiperm.spi.ApiPermProvider;
import top.mddata.base.base.R;
import top.mddata.base.fieldperm.engine.BuiltinMasker;
import top.mddata.base.fieldperm.model.FieldRule;
import top.mddata.base.fieldperm.model.UserFieldPerm;
import top.mddata.base.fieldperm.spi.FieldPermProvider;
import top.mddata.base.idempotent.filter.IdempotencyRequestFilter;
import top.mddata.base.idempotent.interceptor.IdempotencyInterceptor;
import top.mddata.base.idempotent.properties.IdempotencyProperties;
import top.mddata.base.idempotent.repository.IdempotencyRepository;
import top.mddata.base.idempotent.repository.IdempotencyRepository.Acquisition;
import top.mddata.base.idempotent.repository.IdempotencyRepository.CachedResponse;
import top.mddata.base.idempotent.repository.IdempotencyRepository.Outcome;
import top.mddata.base.idempotent.support.IdempotencyConstants;
import top.mddata.base.idempotent.support.IdempotencyResponseWriter;
import top.mddata.base.mvcflex.advice.FieldPermAdvice;
import top.mddata.base.util.ContextUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 两个真实 Advice 与真实幂等重放链的字段脱敏契约。 */
public class IdempotencyFieldPermIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<CachedResponse> cached = new AtomicReference<>();
    private final AtomicInteger controllerCalls = new AtomicInteger();

    private IdempotencyRepository repository;
    private FieldPermProvider fieldPermProvider;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ContextUtil.setUserId(9L);
        repository = mock(IdempotencyRepository.class);
        when(repository.acquire(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            CachedResponse response = cached.get();
            return response == null
                    ? new Acquisition(Outcome.ACQUIRED, "field-execution", null)
                    : new Acquisition(Outcome.REPLAY, null, response);
        });
        doAnswer(invocation -> {
            cached.set(invocation.getArgument(2));
            return null;
        }).when(repository).complete(anyString(), anyString(), any());

        fieldPermProvider = mock(FieldPermProvider.class);
        when(fieldPermProvider.isAuthEnabled()).thenReturn(true);
        ApiPermProvider apiPermProvider = new StubApiPermProvider();
        FieldPermAdvice fieldPermAdvice = new FieldPermAdvice(fieldPermProvider, apiPermProvider);
        IdempotencyResponseAdvice idempotencyAdvice = new IdempotencyResponseAdvice(repository, objectMapper);
        IdempotencyResponseWriter writer = new IdempotencyResponseWriter(objectMapper);
        IdempotencyInterceptor interceptor = new IdempotencyInterceptor(repository, writer);

        // 故意按反序注册，验证 @Order 而不是注册偶然顺序决定脱敏与缓存先后
        mockMvc = MockMvcBuilders.standaloneSetup(new SensitiveController(controllerCalls))
                .setControllerAdvice(idempotencyAdvice, fieldPermAdvice, new FixtureExceptionHandler())
                .addInterceptors(interceptor)
                .addFilters(new IdempotencyRequestFilter(new IdempotencyProperties(), writer))
                .build();
    }

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    void firstResponseCacheAndReplayUseSameMaskedBody() throws Exception {
        stubPhoneMasking();

        String firstBody = performSensitiveRequest()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("138****5678"))
                .andReturn().getResponse().getContentAsString();

        assertEquals(objectMapper.readTree(firstBody), objectMapper.readTree(cached.get().body()));
        String replayBody = performSensitiveRequest()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("138****5678"))
                .andReturn().getResponse().getContentAsString();
        assertEquals(objectMapper.readTree(firstBody), objectMapper.readTree(replayBody));
        assertEquals(1, controllerCalls.get());
    }

    @Test
    void fieldPermissionFailureThenGlobalErrorNeverReleases() throws Exception {
        when(fieldPermProvider.findMenuId("/fixture/sensitive", "POST"))
                .thenThrow(new IllegalStateException("字段权限处理失败"))
                .thenReturn(null);

        performSensitiveRequest()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(-10));

        verify(repository, never()).release(anyString(), anyString());
        verify(repository, never()).complete(anyString(), anyString(), any());
    }

    private void stubPhoneMasking() {
        when(fieldPermProvider.findMenuId("/fixture/sensitive", "POST")).thenReturn(100L);
        when(fieldPermProvider.findUserPerm(9L)).thenReturn(new UserFieldPerm(false, Map.of(
                100L, Map.of("phone", FieldRule.mask(BuiltinMasker.MOBILE)))));
    }

    private org.springframework.test.web.servlet.ResultActions performSensitiveRequest() throws Exception {
        return mockMvc.perform(post("/fixture/sensitive")
                .header(IdempotencyConstants.HEADER, "field-test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));
    }

    public static class SensitiveData {
        private String phone;

        public SensitiveData(String phone) {
            this.phone = phone;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    @RestController
    @Profile("idempotency-mvc-test")
    private static class SensitiveController {
        private final AtomicInteger calls;

        private SensitiveController(AtomicInteger calls) {
            this.calls = calls;
        }

        @Idempotent
        @PostMapping("/fixture/sensitive")
        R<SensitiveData> save() {
            calls.incrementAndGet();
            return R.success(new SensitiveData("13812345678"));
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

    private static class StubApiPermProvider implements ApiPermProvider {
        @Override
        public boolean isAuthEnabled() {
            return true;
        }

        @Override
        public boolean isNotConfigAllow() {
            return false;
        }

        @Override
        public String getGatewayPrefix() {
            return "api";
        }

        @Override
        public Set<String> getServicePrefixes() {
            return Set.of("console");
        }

        @Override
        public List<top.mddata.base.apiperm.model.ApiPattern> findAllPatterns() {
            return List.of();
        }

        @Override
        public UserApiPerm findUserPerm(Long userId) {
            return new UserApiPerm(false, Set.of());
        }
    }
}
