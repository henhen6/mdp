package top.mddata.workbench.service.social.impl;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.mddata.base.exception.BizException;
import top.mddata.common.entity.User;
import top.mddata.common.entity.UserSocialAuth;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.SocialCallbackDto;
import top.mddata.workbench.dto.SocialReauthDto;
import top.mddata.workbench.dto.SocialReauthUrlDto;
import top.mddata.workbench.dto.SocialTicketDto;
import top.mddata.workbench.dto.SocialTransferDto;
import top.mddata.workbench.dto.SocialUnbindDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.SocialActionEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.enumeration.SocialReauthMethodEnum;
import top.mddata.workbench.service.AccountAuthenticator;
import top.mddata.workbench.service.LoginSessionService;
import top.mddata.workbench.service.UserSocialAuthService;
import top.mddata.workbench.service.social.SocialAccountService;
import top.mddata.workbench.service.social.SocialProvider;
import top.mddata.workbench.service.social.SocialProviderFactory;
import top.mddata.workbench.service.social.SocialTicketService;
import top.mddata.workbench.service.social.model.SocialIdentity;
import top.mddata.workbench.service.social.model.SocialReauth;
import top.mddata.workbench.service.social.model.SocialState;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.SocialBindingVo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SocialAccountServiceImpl} 个人中心账号管理行为单测。
 *
 * @author tangyh
 */
class SocialAccountServiceImplTest {

    private SocialTicketService ticketService;
    private UserSocialAuthService userSocialAuthService;
    private AccountAuthenticator accountAuthenticator;
    private SocialProviderFactory providerFactory;
    private SocialProvider provider;
    private LoginSessionService loginSessionService;
    private SocialAccountService service;

    private MockedStatic<StpUtil> stpMock;

    @BeforeEach
    void setUp() {
        ticketService = mock(SocialTicketService.class);
        userSocialAuthService = mock(UserSocialAuthService.class);
        accountAuthenticator = mock(AccountAuthenticator.class);
        providerFactory = mock(SocialProviderFactory.class);
        provider = mock(SocialProvider.class);
        loginSessionService = mock(LoginSessionService.class);
        service = new SocialAccountServiceImpl(ticketService, userSocialAuthService,
                accountAuthenticator, providerFactory, loginSessionService);
        stpMock = mockStatic(StpUtil.class);
    }

    @AfterEach
    void tearDown() {
        stpMock.close();
    }

    @Test
    void 账号凭据属于其他用户时不能签发二次验证票据() {
        User other = new User();
        other.setId(2L);
        when(accountAuthenticator.authenticate(any())).thenReturn(other);

        assertThrows(BizException.class, () -> service.reauth(1L, reauthDto()));

        verify(ticketService, never()).createReauth(any());
    }

    @Test
    void 未绑定平台不允许发起第三方重新授权() {
        when(userSocialAuthService.findByUserAndProvider(1L, SocialProviderEnum.QQ))
                .thenReturn(Optional.empty());

        assertThrows(BizException.class, () -> service.reauthUrl(1L,
                SocialProviderEnum.QQ, reauthUrlDto()));

        verify(ticketService, never()).createState(any());
    }

    @Test
    void 第三方重新授权身份必须等于当前已有绑定() {
        UserSocialAuth current = new UserSocialAuth();
        current.setUserId(1L);
        current.setClientId("client-a");
        current.setOpenId("open-a");
        SocialIdentity scanned = identity().setClientId("client-b").setOpenId("open-b");
        when(ticketService.consumeState("state", SocialProviderEnum.QQ,
                SocialActionEnum.REAUTH)).thenReturn(reauthState());
        when(providerFactory.get(SocialProviderEnum.QQ)).thenReturn(provider);
        when(provider.exchange("code", "state")).thenReturn(scanned);
        when(userSocialAuthService.findByUserAndProvider(1L, SocialProviderEnum.QQ))
                .thenReturn(Optional.of(current));

        assertThrows(BizException.class,
                () -> service.reauthByCallback(1L, callback()));

        verify(ticketService, never()).createReauth(any());
    }

    @Test
    void 绑定回调先校验票据再交换身份并绑定() {
        SocialState state = SocialState.builder()
                .provider(SocialProviderEnum.QQ)
                .action(SocialActionEnum.BIND)
                .userId(1L)
                .reauthTicketId("reauth-ticket")
                .build();
        when(ticketService.consumeState("state", SocialProviderEnum.QQ,
                SocialActionEnum.BIND)).thenReturn(state);
        when(providerFactory.get(SocialProviderEnum.QQ)).thenReturn(provider);
        when(provider.exchange("code", "state")).thenReturn(identity());

        service.bind(1L, callback());

        verify(ticketService).getReauth("reauth-ticket", 1L,
                SocialActionEnum.BIND, SocialProviderEnum.QQ);
        verify(ticketService).consumeReauth("reauth-ticket");
        verify(userSocialAuthService).bind(1L, identity());
    }

    @Test
    void 绑定前二次验证票据用途不符时拒绝() {
        when(ticketService.getReauth("ticket", 1L, SocialActionEnum.BIND, SocialProviderEnum.QQ))
                .thenThrow(new BizException("身份验证票据与当前操作不匹配"));

        assertThrows(BizException.class, () -> service.bindUrl(1L,
                SocialProviderEnum.QQ, ticketDto()));

        verify(ticketService, never()).createState(any());
    }

    @Test
    void 列表按启用平台分组并标记绑定状态() {
        when(providerFactory.enabledProviders()).thenReturn(List.of(
                SocialProviderEnum.QQ, SocialProviderEnum.WECHAT_OPEN));
        UserSocialAuth qq = new UserSocialAuth();
        qq.setProvider(SocialProviderEnum.QQ.name());
        qq.setNickname("QQ用户");
        when(userSocialAuthService.listByUser(1L)).thenReturn(List.of(qq));

        List<SocialBindingVo> result = service.list(1L);

        assertEquals(2, result.size());
        SocialBindingVo qqVo = result.get(0);
        assertTrue(qqVo.isBound());
        assertEquals("QQ用户", qqVo.getNickname());
        assertEquals("qq", qqVo.getProvider());
        SocialBindingVo wechat = result.get(1);
        assertFalse(wechat.isBound());
        assertEquals("wechat-open", wechat.getProvider());
        assertNotNull(result);
    }

    @Test
    void 第三方验证后解绑最后一个身份必须拒绝() {
        when(ticketService.getReauth("ticket", 1L,
                SocialActionEnum.UNBIND, SocialProviderEnum.QQ))
                .thenReturn(boundReauth(SocialReauthMethodEnum.SOCIAL));
        when(userSocialAuthService.findByUserAndProvider(1L, SocialProviderEnum.QQ))
                .thenReturn(Optional.of(binding()));
        when(userSocialAuthService.countByUser(1L)).thenReturn(1L);

        assertThrows(BizException.class, () -> service.unbind(1L, unbindDto()));

        verify(ticketService, never()).consumeReauth(any());
        verify(userSocialAuthService, never()).unbind(any(), any());
    }

    @Test
    void 已验证密码时允许解绑最后一个第三方身份() {
        when(ticketService.getReauth("ticket", 1L,
                SocialActionEnum.UNBIND, SocialProviderEnum.QQ))
                .thenReturn(boundReauth(SocialReauthMethodEnum.PASSWORD));
        when(userSocialAuthService.findByUserAndProvider(1L, SocialProviderEnum.QQ))
                .thenReturn(Optional.of(binding()));
        when(userSocialAuthService.countByUser(1L)).thenReturn(1L);

        service.unbind(1L, unbindDto());

        verify(ticketService).consumeReauth("ticket");
        verify(userSocialAuthService).unbind(binding().getId(), 1L);
    }

    @Test
    void 目标账号与当前账号相同时拒绝转移() {
        when(ticketService.getReauth("ticket", 1L,
                SocialActionEnum.TRANSFER, SocialProviderEnum.QQ))
                .thenReturn(boundReauth(SocialReauthMethodEnum.PASSWORD));
        when(userSocialAuthService.findByUserAndProvider(1L, SocialProviderEnum.QQ))
                .thenReturn(Optional.of(binding()));
        User target = new User();
        target.setId(1L);
        when(accountAuthenticator.authenticate(any())).thenReturn(target);

        assertThrows(BizException.class, () -> service.transfer(1L, transferDto()));

        verify(ticketService, never()).consumeReauth(any());
        verify(userSocialAuthService, never()).transfer(any(), any(), any(), any());
    }

    @Test
    void 目标账号已绑定同平台时拒绝转移() {
        when(ticketService.getReauth("ticket", 1L,
                SocialActionEnum.TRANSFER, SocialProviderEnum.QQ))
                .thenReturn(boundReauth(SocialReauthMethodEnum.PASSWORD));
        when(userSocialAuthService.findByUserAndProvider(1L, SocialProviderEnum.QQ))
                .thenReturn(Optional.of(binding()));
        User target = new User();
        target.setId(2L);
        when(accountAuthenticator.authenticate(any())).thenReturn(target);
        when(userSocialAuthService.findByUserAndProvider(2L, SocialProviderEnum.QQ))
                .thenReturn(Optional.of(new UserSocialAuth()));

        assertThrows(BizException.class, () -> service.transfer(1L, transferDto()));

        verify(ticketService, never()).consumeReauth(any());
        verify(userSocialAuthService, never()).transfer(any(), any(), any(), any());
    }

    @Test
    void 转移只修改绑定并切换到目标账号() {
        when(ticketService.getReauth("ticket", 1L,
                SocialActionEnum.TRANSFER, SocialProviderEnum.QQ))
                .thenReturn(boundReauth(SocialReauthMethodEnum.PASSWORD));
        when(userSocialAuthService.findByUserAndProvider(1L, SocialProviderEnum.QQ))
                .thenReturn(Optional.of(binding()));
        User target = targetUser();
        when(accountAuthenticator.authenticate(any())).thenReturn(target);
        when(userSocialAuthService.findByUserAndProvider(2L, SocialProviderEnum.QQ))
                .thenReturn(Optional.empty());
        LoginVo loginVo = new LoginVo();
        when(loginSessionService.loginAuthenticated(eq(target), eq(AuthTypeEnum.QQ),
                eq("target"), eq("device-id"), eq("device-info"), eq("PC")))
                .thenReturn(loginVo);

        LoginVo result = service.transfer(1L, transferDto());

        assertSame(loginVo, result);
        verify(ticketService).consumeReauth("ticket");
        verify(userSocialAuthService).transfer(binding().getId(), 1L, 2L, 1L);
    }

    private User targetUser() {
        User target = new User();
        target.setId(2L);
        target.setUsername("target");
        target.setState(true);
        return target;
    }

    private SocialTransferDto transferDto() {
        SocialTransferDto dto = new SocialTransferDto();
        dto.setProvider(SocialProviderEnum.QQ);
        dto.setReauthTicket("ticket");
        LoginDto targetLogin = new LoginDto();
        targetLogin.setAuthType(AuthTypeEnum.USERNAME);
        targetLogin.setUsername("target");
        targetLogin.setPassword("password");
        targetLogin.setDeviceId("device-id");
        targetLogin.setDeviceInfo("device-info");
        dto.setTargetLogin(targetLogin);
        return dto;
    }

    private SocialReauth boundReauth(SocialReauthMethodEnum method) {
        return SocialReauth.builder().userId(1L)
                .action(SocialActionEnum.UNBIND)
                .provider(SocialProviderEnum.QQ)
                .method(method)
                .build();
    }

    private UserSocialAuth binding() {
        UserSocialAuth binding = new UserSocialAuth();
        binding.setId(9L);
        binding.setUserId(1L);
        binding.setProvider(SocialProviderEnum.QQ.name());
        return binding;
    }

    private SocialUnbindDto unbindDto() {
        SocialUnbindDto dto = new SocialUnbindDto();
        dto.setProvider(SocialProviderEnum.QQ);
        dto.setReauthTicket("ticket");
        return dto;
    }

    private SocialReauthDto reauthDto() {
        SocialReauthDto dto = new SocialReauthDto();
        dto.setAction(SocialActionEnum.BIND);
        dto.setProvider(SocialProviderEnum.QQ);
        LoginDto login = new LoginDto();
        login.setAuthType(AuthTypeEnum.USERNAME);
        login.setUsername("current");
        login.setPassword("password");
        dto.setLogin(login);
        return dto;
    }

    private SocialReauthUrlDto reauthUrlDto() {
        SocialReauthUrlDto dto = new SocialReauthUrlDto();
        dto.setAction(SocialActionEnum.BIND);
        dto.setProvider(SocialProviderEnum.QQ);
        return dto;
    }

    private SocialTicketDto ticketDto() {
        SocialTicketDto dto = new SocialTicketDto();
        dto.setReauthTicket("ticket");
        return dto;
    }

    private SocialCallbackDto callback() {
        SocialCallbackDto dto = new SocialCallbackDto();
        dto.setProvider(SocialProviderEnum.QQ);
        dto.setCode("code");
        dto.setState("state");
        return dto;
    }

    private SocialState reauthState() {
        return SocialState.builder()
                .provider(SocialProviderEnum.QQ)
                .action(SocialActionEnum.REAUTH)
                .userId(1L)
                .requestedAction(SocialActionEnum.BIND)
                .requestedProvider(SocialProviderEnum.QQ)
                .build();
    }

    private SocialIdentity identity() {
        return SocialIdentity.builder().provider(SocialProviderEnum.QQ)
                .clientId("client-a").openId("open-a").nickname("测试用户").build();
    }
}