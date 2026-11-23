package top.mddata.workbench.handler.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import top.mddata.base.exception.BizException;
import top.mddata.common.constant.ConfigKey;
import top.mddata.common.entity.User;
import top.mddata.common.enumeration.organization.UserSourceEnum;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.facade.system.ConfigFacade;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.event.LoginEvent;
import top.mddata.workbench.service.user.SsoUserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UsernameLoginStrategyImpl} 用户名密码登录策略单测。
 *
 * <p>核心约束：密码过期门禁必须在密码摘要比对通过之后执行，
 * 密码错误时不得泄露过期状态，也不得提前读取过期配置；
 * 安全策略读取失败时必须 fail-closed 拒绝登录。</p>
 *
 * @author tangyh
 */
class UsernameLoginStrategyImplTest {

    private static final String EXPIRE_KEY = ConfigKey.Workbench.PASSWORD_EXPIRE_NOT_ALLOW_LOGIN;
    private static final String EXPIRED_MSG = "密码已过期，请通过忘记密码重置后再登录";
    private static final String CONFIG_READ_FAIL_MSG = "读取密码安全策略失败，请稍后重试";

    @Mock
    private SystemProperties systemProperties;
    @Mock
    private SsoUserService ssoUserService;
    @Mock
    private ConfigFacade configFacade;
    @InjectMocks
    private UsernameLoginStrategyImpl strategy;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        // workbench-service 仅依赖 mockito-core，手动初始化与 MockitoExtension 等价
        mocks = MockitoAnnotations.openMocks(this);
        when(systemProperties.getVerifyPassword()).thenReturn(true);
        // 未配置账号锁定，避免 mock 返回 null 触发拆箱 NPE，聚焦密码过期门禁
        when(configFacade.getInteger(ConfigKey.Workbench.PASSWORD_ERROR_LOCK_ACCOUNT, 0))
                .thenReturn(0);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldNotRevealExpiryWhenPasswordIsWrong() {
        User user = passwordUser();
        user.setPwExpireTime(LocalDateTime.now().minusDays(1));
        LoginDto login = login("wrong");

        try (MockedStatic<SpringUtil> mocked = Mockito.mockStatic(SpringUtil.class)) {
            BizException error = assertThrows(BizException.class,
                    () -> strategy.checkUserPassword(login, user));
            // 精确断言密码错误异常码，绝不能泄露 -17 过期状态
            assertEquals(-1, error.getCode());
            assertNotEquals(-17, error.getCode());
            verify(configFacade, never()).getBooleanRequired(anyString(), anyBoolean());
        }
    }

    @Test
    void shouldRejectExpiredPasswordAfterPasswordVerified() {
        User user = passwordUser();
        user.setPwExpireTime(LocalDateTime.now().minusSeconds(1));
        when(configFacade.getBooleanRequired(EXPIRE_KEY, false)).thenReturn(true);

        try (MockedStatic<SpringUtil> mocked = Mockito.mockStatic(SpringUtil.class)) {
            BizException error = assertThrows(BizException.class,
                    () -> strategy.checkUserPassword(login("correct"), user));
            assertEquals(-17, error.getCode());
            assertEquals(EXPIRED_MSG, error.getMessage());

            ArgumentCaptor<LoginEvent> eventCaptor = ArgumentCaptor.forClass(LoginEvent.class);
            mocked.verify(() -> SpringUtil.publishEvent(eventCaptor.capture()));
            LoginLogDto logDto = (LoginLogDto) eventCaptor.getValue().getSource();
            assertEquals(EXPIRED_MSG, logDto.getStatusReason());
            assertEquals("tester", logDto.getAccount());
        }
    }

    @Test
    void shouldAllowExpiredPasswordWhenSwitchIsOff() {
        User user = passwordUser();
        user.setPwExpireTime(LocalDateTime.now().minusSeconds(1));
        when(configFacade.getBooleanRequired(EXPIRE_KEY, false)).thenReturn(false);
        assertDoesNotThrow(() -> strategy.checkUserPassword(login("correct"), user));
    }

    @Test
    void shouldRejectWhenExpireTimeExactlyNow() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 11, 23, 12, 0, 0);
        UsernameLoginStrategyImpl fixedStrategy = new UsernameLoginStrategyImpl(
                systemProperties, ssoUserService, configFacade) {
            @Override
            protected LocalDateTime now() {
                return fixedNow;
            }
        };
        User user = passwordUser();
        // 到期时刻与当前时刻完全相等，按“到期即拒绝”语义必须拦截
        user.setPwExpireTime(fixedNow);
        when(configFacade.getBooleanRequired(EXPIRE_KEY, false)).thenReturn(true);

        try (MockedStatic<SpringUtil> mocked = Mockito.mockStatic(SpringUtil.class)) {
            BizException error = assertThrows(BizException.class,
                    () -> fixedStrategy.checkUserPassword(login("correct"), user));
            assertEquals(-17, error.getCode());
        }
    }

    @Test
    void shouldAllowWhenSwitchOnButNoExpireTime() {
        User user = passwordUser();
        user.setPwExpireTime(null);
        when(configFacade.getBooleanRequired(EXPIRE_KEY, false)).thenReturn(true);
        assertDoesNotThrow(() -> strategy.checkUserPassword(login("correct"), user));
    }

    @Test
    void shouldDenyLoginWhenConfigReadFails() {
        User user = passwordUser();
        user.setPwExpireTime(LocalDateTime.now().minusSeconds(1));
        when(configFacade.getBooleanRequired(EXPIRE_KEY, false))
                .thenThrow(new RuntimeException("config service down"));

        BizException error = assertThrows(BizException.class,
                () -> strategy.checkUserPassword(login("correct"), user));
        assertEquals(CONFIG_READ_FAIL_MSG, error.getMessage());
    }

    /**
     * 构造平台用户：随机盐、零密码错误次数，密码为 correct 加盐后的摘要。
     */
    private User passwordUser() {
        String salt = RandomUtil.randomString(16);
        User user = new User();
        user.setSalt(salt);
        user.setUserSource(UserSourceEnum.PLATFORM.getCode());
        user.setPwErrorNum(0);
        user.setUsername("tester");
        user.setPassword(SecureUtil.sha256("correct" + salt));
        return user;
    }

    private LoginDto login(String password) {
        LoginDto login = new LoginDto();
        login.setUsername("tester");
        login.setPassword(password);
        return login;
    }
}
