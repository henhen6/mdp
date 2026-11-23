package top.mddata.console.facade.impl.system;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import top.mddata.base.base.R;
import top.mddata.base.exception.BizException;
import top.mddata.console.facade.api.system.ConfigApi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ConfigFacadeImpl#getBooleanRequired(String, Boolean)} 微服务实现单测。
 *
 * <p>安全敏感读取必须 fail-closed：远程响应为 null 或失败 R 时抛业务异常，
 * 绝不回退默认值静默放行；成功且 data 非空时原样返回，data 为空时按契约返回 defaultValue。</p>
 *
 * @author tangyh
 */
class ConfigFacadeImplRequiredBooleanTest {

    private static final String UNIQ_KEY = "PASSWORD_EXPIRE_NOT_ALLOW_LOGIN";

    @Mock
    private ConfigApi configApi;

    private ConfigFacadeImpl facade;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        facade = new ConfigFacadeImpl(configApi);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void 失败响应必须抛异常而不是回退默认值() {
        when(configApi.getBoolean(UNIQ_KEY, false))
                .thenReturn(R.fail(-1, "配置服务不可用"));

        BizException error = assertThrows(BizException.class,
                () -> facade.getBooleanRequired(UNIQ_KEY, false));
        assertEquals("读取系统安全策略失败，请稍后重试", error.getMessage());
    }

    @Test
    void 空响应必须抛异常而不是放行() {
        when(configApi.getBoolean(UNIQ_KEY, false)).thenReturn(null);

        BizException error = assertThrows(BizException.class,
                () -> facade.getBooleanRequired(UNIQ_KEY, false));
        assertEquals("读取系统安全策略失败，请稍后重试", error.getMessage());
    }

    @Test
    void 远程调用异常必须向上抛出() {
        when(configApi.getBoolean(UNIQ_KEY, false))
                .thenThrow(new RuntimeException("feign timeout"));

        assertThrows(RuntimeException.class,
                () -> facade.getBooleanRequired(UNIQ_KEY, false));
    }

    @Test
    void 成功响应原样返回数据并透传参数() {
        when(configApi.getBoolean(UNIQ_KEY, false)).thenReturn(R.success(true));

        Boolean value = facade.getBooleanRequired(UNIQ_KEY, false);

        assertTrue(value);
        verify(configApi).getBoolean(UNIQ_KEY, false);
    }

    @Test
    void 成功响应但数据为空时返回默认值() {
        when(configApi.getBoolean(UNIQ_KEY, false)).thenReturn(R.success(null));

        Boolean value = facade.getBooleanRequired(UNIQ_KEY, false);

        assertEquals(false, value);
        verify(configApi).getBoolean(UNIQ_KEY, false);
    }
}
