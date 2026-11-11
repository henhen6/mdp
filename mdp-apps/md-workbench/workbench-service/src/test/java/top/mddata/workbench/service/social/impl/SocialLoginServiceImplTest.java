package top.mddata.workbench.service.social.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import top.mddata.base.exception.BizException;
import top.mddata.common.entity.User;
import top.mddata.common.entity.UserSocialAuth;
import top.mddata.workbench.dto.SocialCallbackDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.SocialActionEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.AccountAuthenticator;
import top.mddata.workbench.service.LoginSessionService;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.service.UserSocialAuthService;
import top.mddata.workbench.service.social.SocialLoginService;
import top.mddata.workbench.service.social.SocialProvider;
import top.mddata.workbench.service.social.SocialProviderFactory;
import top.mddata.workbench.service.social.SocialTicketService;
import top.mddata.workbench.service.social.model.SocialIdentity;
import top.mddata.workbench.service.social.model.SocialState;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.SocialLoginResultVo;
import top.mddata.workbench.vo.SocialProviderVo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link SocialLoginServiceImpl} 登录编排行为单测。
 *
 * <p>覆盖已绑定身份直接登录、未绑定身份只签发待选择票据、state 先消费后换码的顺序、
 * state/换码/用户状态异常路径、平台列表与授权地址生成，以及三平台到认证类型的显式映射。</p>
 *
 * @author tangyh
 */
class SocialLoginServiceImplTest {

    private SocialTicketService ticketService;
    private SocialProviderFactory providerFactory;
    private SocialProvider provider;
    private UserSocialAuthService userSocialAuthService;
    private SsoUserService ssoUserService;
    private AccountAuthenticator accountAuthenticator;
    private LoginSessionService loginSessionService;
    private SocialLoginService service;

    @BeforeEach
    void setUp() {
        ticketService = mock(SocialTicketService.class);
        providerFactory = mock(SocialProviderFactory.class);
        provider = mock(SocialProvider.class);
        userSocialAuthService = mock(UserSocialAuthService.class);
        ssoUserService = mock(SsoUserService.class);
        accountAuthenticator = mock(AccountAuthenticator.class);
        loginSessionService = mock(LoginSessionService.class);
        service = new SocialLoginServiceImpl(ticketService, providerFactory, userSocialAuthService,
                ssoUserService, accountAuthenticator, loginSessionService);

        when(providerFactory.get(SocialProviderEnum.QQ)).thenReturn(provider);
    }

    @Test
    void 已绑定身份直接登录原用户() {
        SocialIdentity identity = identity();
        UserSocialAuth binding = binding();
        User user = user();
        when(provider.exchange("code", "state")).thenReturn(identity);
        when(userSocialAuthService.findByIdentity(identity)).thenReturn(Optional.of(binding));
        when(ssoUserService.getById(7L)).thenReturn(user);
        when(loginSessionService.loginAuthenticated(any(), any(), any(), any(), any(), any()))
                .thenReturn(new LoginVo());

        SocialLoginResultVo result = service.login(callback());

        assertEquals("SUCCESS", result.getStatus());
        assertNotNull(result.getLogin());
        assertNull(result.getPendingTicket());
        assertNull(result.getProvider());
        assertNull(result.getNickname());
        verify(accountAuthenticator).checkUserState(eq(user), eq(AuthTypeEnum.QQ), isNull());
        verify(loginSessionService).loginAuthenticated(eq(user), eq(AuthTypeEnum.QQ),
                eq("existing"), eq("device-id"), isNull(), eq("PC"));
        verify(ticketService, never()).createPending(any());
    }

    @Test
    void 未绑定身份只签发待选择票据不创建用户() {
        when(provider.exchange(any(), any())).thenReturn(identity());
        when(userSocialAuthService.findByIdentity(any())).thenReturn(Optional.empty());
        when(ticketService.createPending(any())).thenReturn("pending-ticket");

        SocialLoginResultVo result = service.login(callback());

        assertEquals("PENDING", result.getStatus());
        assertEquals("pending-ticket", result.getPendingTicket());
        assertEquals("qq", result.getProvider());
        assertEquals("测试用户", result.getNickname());
        assertNull(result.getLogin());
        verifyNoInteractions(loginSessionService, ssoUserService);
        verify(userSocialAuthService, never()).bind(anyLong(), any());
    }

    @Test
    void state校验失败时不换取身份也不查询绑定() {
        doThrow(new BizException("授权状态已失效"))
                .when(ticketService).consumeState(anyString(), any(), any());

        assertThrows(BizException.class, () -> service.login(callback()));

        verify(provider, never()).exchange(anyString(), anyString());
        verify(userSocialAuthService, never()).findByIdentity(any());
        verify(ticketService, never()).createPending(any());
        verifyNoInteractions(loginSessionService);
    }

    @Test
    void 授权码换取身份失败时不签发待选择票据() {
        when(provider.exchange(any(), any())).thenThrow(new BizException("第三方授权失败"));

        assertThrows(BizException.class, () -> service.login(callback()));

        verify(userSocialAuthService, never()).findByIdentity(any());
        verify(ticketService, never()).createPending(any());
        verifyNoInteractions(loginSessionService);
    }

    @Test
    void 已绑定用户状态异常时拒绝登录不签发会话() {
        SocialIdentity identity = identity();
        UserSocialAuth binding = binding();
        User user = user();
        when(provider.exchange(any(), any())).thenReturn(identity);
        when(userSocialAuthService.findByIdentity(identity)).thenReturn(Optional.of(binding));
        when(ssoUserService.getById(7L)).thenReturn(user);
        doThrow(new BizException("账号已禁用"))
                .when(accountAuthenticator).checkUserState(any(), any(), any());

        assertThrows(BizException.class, () -> service.login(callback()));

        verifyNoInteractions(loginSessionService);
        verify(ticketService, never()).createPending(any());
    }

    @Test
    void 先消费state再用授权码换取身份() {
        when(provider.exchange(any(), any())).thenReturn(identity());
        service.login(callback());

        InOrder inOrder = inOrder(ticketService, provider);
        inOrder.verify(ticketService).consumeState("state", SocialProviderEnum.QQ,
                SocialActionEnum.LOGIN);
        inOrder.verify(provider).exchange("code", "state");
    }

    @Test
    void 列出已启用平台时返回平台编码和显示名称() {
        when(providerFactory.enabledProviders())
                .thenReturn(List.of(SocialProviderEnum.QQ, SocialProviderEnum.DINGTALK));

        List<SocialProviderVo> result = service.providers();

        assertEquals(2, result.size());
        assertEquals("qq", result.get(0).getCode());
        assertEquals("QQ", result.get(0).getLabel());
        assertEquals("dingtalk", result.get(1).getCode());
        assertEquals("钉钉", result.get(1).getLabel());
    }

    @Test
    void 生成登录地址时签发LOGIN动作state并返回授权地址() {
        when(ticketService.createState(any())).thenReturn("state-ticket");
        when(provider.authorize("state-ticket")).thenReturn("https://authorize.url");

        String url = service.loginUrl(SocialProviderEnum.QQ);

        assertEquals("https://authorize.url", url);
        ArgumentCaptor<SocialState> captor = ArgumentCaptor.forClass(SocialState.class);
        verify(ticketService).createState(captor.capture());
        SocialState state = captor.getValue();
        assertEquals(SocialProviderEnum.QQ, state.getProvider());
        assertEquals(SocialActionEnum.LOGIN, state.getAction());
        assertNull(state.getUserId());
    }

    @Test
    void 所有平台通过枚举元数据映射到对应认证类型() {
        for (SocialProviderEnum providerEnum : SocialProviderEnum.values()) {
            SocialProvider adapter = mock(SocialProvider.class);
            when(providerFactory.get(providerEnum)).thenReturn(adapter);
            SocialIdentity identity = SocialIdentity.builder().provider(providerEnum)
                    .clientId("client").openId("open").nickname("用户").build();
            UserSocialAuth binding = binding();
            User user = user();
            when(adapter.exchange(any(), any())).thenReturn(identity);
            when(userSocialAuthService.findByIdentity(identity)).thenReturn(Optional.of(binding));
            when(ssoUserService.getById(7L)).thenReturn(user);
            when(loginSessionService.loginAuthenticated(any(), any(), any(), any(), any(), any()))
                    .thenReturn(new LoginVo());

            service.login(callback(providerEnum));

            AuthTypeEnum expected = providerEnum.getAuthType();
            verify(loginSessionService).loginAuthenticated(eq(user), eq(expected), eq("existing"),
                    eq("device-id"), isNull(), eq("PC"));
        }
    }

    private UserSocialAuth binding() {
        UserSocialAuth binding = new UserSocialAuth();
        binding.setUserId(7L);
        return binding;
    }

    private User user() {
        User user = new User();
        user.setId(7L);
        user.setUsername("existing");
        return user;
    }

    private SocialIdentity identity() {
        return SocialIdentity.builder().provider(SocialProviderEnum.QQ)
                .clientId("client-id").openId("open-id").nickname("测试用户").build();
    }

    private SocialCallbackDto callback() {
        return callback(SocialProviderEnum.QQ);
    }

    private SocialCallbackDto callback(SocialProviderEnum provider) {
        SocialCallbackDto callback = new SocialCallbackDto();
        callback.setProvider(provider);
        callback.setCode("code");
        callback.setState("state");
        callback.setDeviceId("device-id");
        return callback;
    }
}
