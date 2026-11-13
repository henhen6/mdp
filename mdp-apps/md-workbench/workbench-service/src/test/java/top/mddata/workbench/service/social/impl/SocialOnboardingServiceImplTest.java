package top.mddata.workbench.service.social.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import top.mddata.base.exception.BizException;
import top.mddata.common.dto.UserSocialRegisterDto;
import top.mddata.common.entity.User;
import top.mddata.common.entity.UserSocialAuth;
import top.mddata.common.properties.SystemProperties;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.console.facade.organization.UserFacade;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.SocialBindExistingDto;
import top.mddata.workbench.dto.SocialRegisterDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.AccountAuthenticator;
import top.mddata.workbench.service.LoginSessionService;
import top.mddata.workbench.service.UserSocialAuthService;
import top.mddata.workbench.service.social.SocialOnboardingService;
import top.mddata.workbench.service.social.SocialTicketService;
import top.mddata.workbench.service.social.model.PendingSocialUser;
import top.mddata.workbench.service.social.model.SocialIdentity;
import top.mddata.workbench.vo.LoginVo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SocialOnboardingServiceImpl} 首次选择行为单测。
 *
 * <p>覆盖绑定已有账号的成功路径、凭据失败累计但不消费票据、目标冲突与身份
 * 冲突快速失败，以及社交注册的协议确认、票据消费顺序、随机用户名与系统
 * 默认密码、远程返回新用户 ID 后自动登录。</p>
 *
 * @author tangyh
 */
class SocialOnboardingServiceImplTest {

    private static final String PENDING_TICKET = "pending-ticket";

    private static final String DEF_PWD = "social-default-pwd";

    private SocialTicketService ticketService;
    private UserSocialAuthService userSocialAuthService;
    private AccountAuthenticator accountAuthenticator;
    private LoginSessionService loginSessionService;
    private UserFacade userFacade;
    private SocialOnboardingService service;

    @BeforeEach
    void setUp() {
        ticketService = mock(SocialTicketService.class);
        userSocialAuthService = mock(UserSocialAuthService.class);
        accountAuthenticator = mock(AccountAuthenticator.class);
        loginSessionService = mock(LoginSessionService.class);
        userFacade = mock(UserFacade.class);
        SystemProperties systemProperties = new SystemProperties();
        systemProperties.setDefPwd(DEF_PWD);
        service = new SocialOnboardingServiceImpl(ticketService, userSocialAuthService,
                accountAuthenticator, loginSessionService, userFacade, systemProperties);

        when(ticketService.getPending(PENDING_TICKET))
                .thenReturn(new PendingSocialUser(identity()));
    }

    @Test
    void 已有账号凭据错误累计失败且不绑定() {
        when(accountAuthenticator.authenticate(any())).thenThrow(new BizException("验证失败"));

        assertThrows(BizException.class, () -> service.bindExisting(bindDto()));

        verify(ticketService).registerPendingFailure(PENDING_TICKET);
        verify(userSocialAuthService, never()).bind(any(), any());
        verify(ticketService, never()).consumePending(PENDING_TICKET);
        verifyNoLoginSession();
    }

    @Test
    void pending票据失效时不调用凭据认证() {
        when(ticketService.getPending(PENDING_TICKET))
                .thenThrow(new BizException("待选择票据已失效"));

        assertThrows(BizException.class, () -> service.bindExisting(bindDto()));

        verify(accountAuthenticator, never()).authenticate(any());
        verify(ticketService, never()).consumePending(PENDING_TICKET);
        verify(ticketService, never()).registerPendingFailure(PENDING_TICKET);
        verify(userSocialAuthService, never()).bind(any(), any());
        verifyNoLoginSession();
    }

    @Test
    void 绑定已有账号先验票再认证凭据() {
        User target = targetUser();
        when(accountAuthenticator.authenticate(any())).thenReturn(target);
        when(userSocialAuthService.findByUserAndProvider(7L, SocialProviderEnum.QQ))
                .thenReturn(Optional.empty());
        when(userSocialAuthService.findByIdentity(identity())).thenReturn(Optional.empty());

        service.bindExisting(bindDto());

        InOrder order = inOrder(ticketService, accountAuthenticator);
        order.verify(ticketService).getPending(PENDING_TICKET);
        order.verify(accountAuthenticator).authenticate(any());
    }

    @Test
    void 绑定已有账号成功后消费票据并签发目标用户会话() {
        User target = targetUser();
        when(accountAuthenticator.authenticate(any())).thenReturn(target);
        when(userSocialAuthService.findByUserAndProvider(7L, SocialProviderEnum.QQ))
                .thenReturn(Optional.empty());
        when(userSocialAuthService.findByIdentity(identity())).thenReturn(Optional.empty());
        when(loginSessionService.loginAuthenticated(any(), any(), any(), any(), any(), any()))
                .thenReturn(new LoginVo());

        LoginVo result = service.bindExisting(bindDto());

        assertNotNull(result);
        verify(ticketService).consumePending(PENDING_TICKET);
        verify(userSocialAuthService).bind(7L, identity());
        verify(loginSessionService).loginAuthenticated(eq(target), eq(AuthTypeEnum.QQ),
                eq("existing"), eq("device-id"), eq("设备信息"), eq("PC"));
        verify(ticketService, never()).registerPendingFailure(PENDING_TICKET);
    }

    @Test
    void 目标账号已绑定同平台时快速失败() {
        User target = targetUser();
        when(accountAuthenticator.authenticate(any())).thenReturn(target);
        when(userSocialAuthService.findByUserAndProvider(7L, SocialProviderEnum.QQ))
                .thenReturn(Optional.of(new UserSocialAuth()));

        assertThrows(Exception.class, () -> service.bindExisting(bindDto()));

        verify(ticketService, never()).consumePending(PENDING_TICKET);
        verify(ticketService, never()).registerPendingFailure(PENDING_TICKET);
        verify(userSocialAuthService, never()).bind(any(), any());
        verifyNoLoginSession();
    }

    @Test
    void 第三方身份已绑定其他用户时快速失败() {
        User target = targetUser();
        when(accountAuthenticator.authenticate(any())).thenReturn(target);
        when(userSocialAuthService.findByUserAndProvider(7L, SocialProviderEnum.QQ))
                .thenReturn(Optional.empty());
        when(userSocialAuthService.findByIdentity(identity()))
                .thenReturn(Optional.of(new UserSocialAuth()));

        assertThrows(Exception.class, () -> service.bindExisting(bindDto()));

        verify(ticketService, never()).consumePending(PENDING_TICKET);
        verify(userSocialAuthService, never()).bind(any(), any());
        verifyNoLoginSession();
    }

    @Test
    void 社交注册不强制联系方式且密码使用系统默认密码() {
        SocialRegisterDto dto = registerDto();
        when(userFacade.registerBySocial(any())).thenReturn(99L);

        service.register(dto);

        ArgumentCaptor<UserSocialRegisterDto> captor =
                ArgumentCaptor.forClass(UserSocialRegisterDto.class);
        verify(userFacade).registerBySocial(captor.capture());
        User user = captor.getValue().getUser();
        assertNull(user.getPhone());
        assertNull(user.getEmail());
        assertNotNull(user.getPassword());
        assertEquals(DEF_PWD, user.getPassword());
        assertFalse(user.getUsername().contains(identity().getOpenId()));
        assertTrue(user.getUsername().startsWith("social_"));
        assertEquals(99L, user.getId());
        assertEquals("测试用户", user.getName());
    }

    @Test
    void 注册成功先消费票据再远程注册并自动登录() {
        SocialRegisterDto dto = registerDto();
        when(userFacade.registerBySocial(any())).thenReturn(99L);
        when(loginSessionService.loginAuthenticated(any(), any(), any(), any(), any(), any()))
                .thenReturn(new LoginVo());

        LoginVo result = service.register(dto);

        assertNotNull(result);
        InOrder order = inOrder(ticketService, userFacade, loginSessionService);
        order.verify(ticketService).consumePending(PENDING_TICKET);
        order.verify(userFacade).registerBySocial(any(UserSocialRegisterDto.class));
        order.verify(loginSessionService).loginAuthenticated(any(), eq(AuthTypeEnum.QQ),
                any(), eq("device-id"), eq("设备信息"), eq("PC"));
    }

    @Test
    void 未同意用户协议时拒绝注册() {
        SocialRegisterDto dto = registerDto();
        dto.setAgreement(false);

        BizException ex = assertThrows(BizException.class, () -> service.register(dto));
        assertTrue(ex.getMessage().contains("用户协议"));

        verify(userFacade, never()).registerBySocial(any());
        verify(ticketService, never()).consumePending(PENDING_TICKET);
        verifyNoLoginSession();
    }

    @Test
    void 姓名为空时默认使用第三方昵称() {
        SocialRegisterDto dto = registerDto();
        dto.setName("  ");
        when(userFacade.registerBySocial(any())).thenReturn(99L);

        service.register(dto);

        ArgumentCaptor<UserSocialRegisterDto> captor =
                ArgumentCaptor.forClass(UserSocialRegisterDto.class);
        verify(userFacade).registerBySocial(captor.capture());
        assertEquals("测试用户", captor.getValue().getUser().getName());
    }

    @Test
    void 远程注册返回空用户ID时快速失败() {
        SocialRegisterDto dto = registerDto();
        when(userFacade.registerBySocial(any())).thenReturn(null);

        assertThrows(Exception.class, () -> service.register(dto));

        // 票据按约定在远程写入前消费，失败后需用户重新发起
        verify(ticketService).consumePending(PENDING_TICKET);
        verifyNoLoginSession();
    }

    private void verifyNoLoginSession() {
        verify(loginSessionService, never())
                .loginAuthenticated(any(), any(), any(), any(), any(), any());
    }

    private SocialBindExistingDto bindDto() {
        SocialBindExistingDto dto = new SocialBindExistingDto();
        dto.setPendingTicket(PENDING_TICKET);
        LoginDto login = new LoginDto();
        login.setAuthType(AuthTypeEnum.USERNAME);
        login.setUsername("existing");
        login.setPassword("password");
        login.setDeviceId("device-id");
        login.setDeviceInfo("设备信息");
        dto.setLogin(login);
        return dto;
    }

    private SocialRegisterDto registerDto() {
        SocialRegisterDto dto = new SocialRegisterDto();
        dto.setPendingTicket(PENDING_TICKET);
        dto.setName("测试用户");
        dto.setNature(OrgNatureEnum.HEAD_COMPANY.getCode());
        dto.setAgreement(true);
        dto.setDeviceId("device-id");
        dto.setDeviceInfo("设备信息");
        return dto;
    }

    private SocialIdentity identity() {
        return SocialIdentity.builder().provider(SocialProviderEnum.QQ)
                .clientId("client-id").openId("open-id").nickname("测试用户").build();
    }

    private User targetUser() {
        User target = new User();
        target.setId(7L);
        target.setUsername("existing");
        return target;
    }
}
