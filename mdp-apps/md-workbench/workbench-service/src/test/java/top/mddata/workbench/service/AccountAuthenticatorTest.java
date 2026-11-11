package top.mddata.workbench.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import top.mddata.base.exception.BizException;
import top.mddata.common.entity.User;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.handler.LoginStrategy;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link AccountAuthenticator} 账号认证组件单测。
 *
 * <p>核心约束：社交认证类型只用于日志，绝不能落入账号密码策略 Map；
 * 账号类认证必须保持原有 checkParam → getUser → checkUserPassword → checkUserState 顺序。</p>
 *
 * @author tangyh
 */
class AccountAuthenticatorTest {

    private LoginStrategy phoneStrategy;
    private LoginStrategy usernameStrategy;
    private AccountAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        phoneStrategy = mock(LoginStrategy.class);
        usernameStrategy = mock(LoginStrategy.class);
        Map<String, LoginStrategy> strategies = Map.of(
                AuthTypeEnum.PHONE.name(), phoneStrategy,
                AuthTypeEnum.USERNAME.name(), usernameStrategy);
        authenticator = new AccountAuthenticator(strategies);
    }

    @Test
    void 社交认证类型不能走账号密码策略() {
        LoginDto login = new LoginDto();
        login.setAuthType(AuthTypeEnum.WECHAT_OPEN);

        assertThrows(BizException.class, () -> authenticator.authenticate(login));
        verifyNoInteractions(phoneStrategy);
        verifyNoInteractions(usernameStrategy);
    }

    @Test
    void 手机认证仍复用现有策略顺序() {
        LoginDto login = new LoginDto();
        login.setAuthType(AuthTypeEnum.PHONE);
        login.setUsername("13800000000");
        User user = new User();
        user.setId(1L);
        when(phoneStrategy.getUser(login.getUsername())).thenReturn(user);

        assertSame(user, authenticator.authenticate(login));

        InOrder order = inOrder(phoneStrategy);
        order.verify(phoneStrategy).checkParam(login);
        order.verify(phoneStrategy).getUser(login.getUsername());
        order.verify(phoneStrategy).checkUserPassword(login, user);
        order.verify(phoneStrategy).checkUserState(login, user);
    }

    @Test
    void 登录参数为空时快速失败() {
        assertThrows(BizException.class, () -> authenticator.authenticate(null));
        verifyNoInteractions(phoneStrategy);
        verifyNoInteractions(usernameStrategy);
    }

    @Test
    void 已认证用户状态校验委托用户名策略并保留社交类型上下文() {
        User user = new User();
        user.setUsername("zhangsan");

        authenticator.checkUserState(user, AuthTypeEnum.WECHAT_OPEN, "device-info");

        ArgumentCaptor<LoginDto> captor = ArgumentCaptor.forClass(LoginDto.class);
        verify(usernameStrategy).checkUserState(captor.capture(), same(user));
        LoginDto context = captor.getValue();
        assertEquals(AuthTypeEnum.WECHAT_OPEN, context.getAuthType());
        assertEquals("zhangsan", context.getUsername());
        assertEquals("device-info", context.getDeviceInfo());
        verifyNoInteractions(phoneStrategy);
    }

    @Test
    void 状态校验用户为空时快速失败() {
        assertThrows(BizException.class,
                () -> authenticator.checkUserState(null, AuthTypeEnum.WECHAT_OPEN, null));
        verifyNoInteractions(usernameStrategy);
    }
}
