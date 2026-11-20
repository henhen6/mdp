package top.mddata.workbench.service.impl;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.dev33.satoken.temp.SaTempUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.mddata.base.base.R;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.exception.BizException;
import top.mddata.common.entity.User;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.facade.message.MsgFacade;
import top.mddata.console.facade.organization.UserFacade;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.TwoFactorVerifyDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.service.AccountAuthenticator;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.service.TwoFactorAuthService;
import top.mddata.workbench.service.TwoFactorTicketService.TwoFactorTicketPayload;
import top.mddata.workbench.service.WxMaAuthService;
import top.mddata.workbench.vo.LoginVo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthServiceImpl} 登录链路双因素分支单测。
 *
 * <p>验证：密码类登录（CAPTCHA/USERNAME）认证通过后，{@link TwoFactorAuthService#shouldTrigger}
 * 为 true 时走 startTwoFactor 返回 TWO_FACTOR 状态且不创建会话、不发登录事件；
 * 为 false 时走既有 loginAuthenticated 签发 TOKEN 状态的 LoginVo。
 * 验证码登录（PHONE/EMAIL）不评估双因素策略，即使 shouldTrigger 为 true 也直接签发 TOKEN。</p>
 *
 * @author tangyh
 */
class AuthServiceImplTwoFactorTest {

    private static final Long USER_ID = 1L;

    private SaTokenConfig saTokenConfig;
    private SsoUserService ssoUserService;
    private AccountAuthenticator accountAuthenticator;
    private TwoFactorAuthService twoFactorAuthService;
    private AuthServiceImpl authService;

    private MockedStatic<StpUtil> stpMock;
    private MockedStatic<SaTempUtil> saTempMock;
    private MockedStatic<SpringUtil> springMock;

    @BeforeEach
    void setUp() {
        saTokenConfig = mock(SaTokenConfig.class);
        ssoUserService = mock(SsoUserService.class);
        accountAuthenticator = mock(AccountAuthenticator.class);
        twoFactorAuthService = mock(TwoFactorAuthService.class);
        authService = new AuthServiceImpl(mock(SystemProperties.class), saTokenConfig,
                mock(CacheOps.class), ssoUserService, mock(UserFacade.class), mock(MsgFacade.class),
                mock(WxMaAuthService.class), accountAuthenticator, twoFactorAuthService);

        stpMock = mockStatic(StpUtil.class);
        saTempMock = mockStatic(SaTempUtil.class);
        springMock = mockStatic(SpringUtil.class);
    }

    @AfterEach
    void tearDown() {
        stpMock.close();
        saTempMock.close();
        springMock.close();
    }

    @Test
    void login_shouldReturnTwoFactorVo_whenTriggered() {
        User user = user();
        // deviceId/deviceInfo 给两个非空且不同的值，防止参数互换无法被测试捕获
        LoginDto login = loginDto(AuthTypeEnum.CAPTCHA, "admin",
                "trigger-device-id", "trigger-device-info");
        LoginVo twoFactorVo = new LoginVo();
        twoFactorVo.setStatus("TWO_FACTOR");
        when(accountAuthenticator.authenticate(login)).thenReturn(user);
        when(twoFactorAuthService.shouldTrigger(user)).thenReturn(true);
        when(twoFactorAuthService.startTwoFactor(user, AuthTypeEnum.CAPTCHA,
                "admin", "trigger-device-id", "trigger-device-info")).thenReturn(twoFactorVo);

        R<LoginVo> result = authService.login(login);

        assertSame(twoFactorVo, result.getData());
        assertEquals("TWO_FACTOR", result.getData().getStatus());
        verify(twoFactorAuthService).startTwoFactor(user, AuthTypeEnum.CAPTCHA,
                "admin", "trigger-device-id", "trigger-device-info");
        // 触发分支只签发票据，不创建会话、不发登录事件
        stpMock.verify(() -> StpUtil.login(anyLong(), any(SaLoginParameter.class)), never());
        springMock.verify(() -> SpringUtil.publishEvent(any()), never());
    }

    @Test
    void login_shouldReturnTokenVo_whenNotTriggered() {
        User user = user();
        LoginDto login = loginDto(AuthTypeEnum.USERNAME, "zhangsan",
                "device-id", "device-info");
        when(accountAuthenticator.authenticate(login)).thenReturn(user);
        when(twoFactorAuthService.shouldTrigger(user)).thenReturn(false);

        // loginAuthenticated 真实执行所需的 Sa-Token 静态桩
        SaSession session = mock(SaSession.class);
        stpMock.when(StpUtil::getTokenSession).thenReturn(session);
        SaTokenInfo tokenInfo = new SaTokenInfo();
        tokenInfo.setTokenTimeout(7200L);
        stpMock.when(StpUtil::getTokenInfo).thenReturn(tokenInfo);
        when(saTokenConfig.getTimeout()).thenReturn(3600L);
        saTempMock.when(() -> SaTempUtil.createToken(anyString(), anyLong()))
                .thenReturn("refresh-token");
        stubEmptyOrg();

        R<LoginVo> result = authService.login(login);

        assertEquals("TOKEN", result.getData().getStatus());
        verify(twoFactorAuthService, never())
                .startTwoFactor(any(), any(), any(), any(), any());
        stpMock.verify(() -> StpUtil.login(eq(USER_ID), any(SaLoginParameter.class)));
    }

    @Test
    void login_shouldBypassTwoFactor_whenPhoneCodeLoginEvenIfTriggered() {
        User user = user();
        LoginDto login = loginDto(AuthTypeEnum.PHONE, "13800000000",
                "phone-device-id", "phone-device-info");
        when(accountAuthenticator.authenticate(login)).thenReturn(user);
        // 故意打桩为 true：短信验证码本身已是持有因素，即使策略命中也不得进入 2FA
        when(twoFactorAuthService.shouldTrigger(user)).thenReturn(true);

        // loginAuthenticated 真实执行所需的 Sa-Token 静态桩
        SaSession session = mock(SaSession.class);
        stpMock.when(StpUtil::getTokenSession).thenReturn(session);
        SaTokenInfo tokenInfo = new SaTokenInfo();
        tokenInfo.setTokenTimeout(7200L);
        stpMock.when(StpUtil::getTokenInfo).thenReturn(tokenInfo);
        when(saTokenConfig.getTimeout()).thenReturn(3600L);
        saTempMock.when(() -> SaTempUtil.createToken(anyString(), anyLong()))
                .thenReturn("refresh-token");
        stubEmptyOrg();

        R<LoginVo> result = authService.login(login);

        assertEquals("TOKEN", result.getData().getStatus());
        // 非密码类登录不评估双因素策略，更不会签发 2FA 票据
        verify(twoFactorAuthService, never()).shouldTrigger(user);
        verify(twoFactorAuthService, never())
                .startTwoFactor(any(), any(), any(), any(), any());
        // 真实走 loginAuthenticated 签发会话
        stpMock.verify(() -> StpUtil.login(eq(USER_ID), any(SaLoginParameter.class)));
    }

    @Test
    void verifyTwoFactorLogin_shouldThrow_whenUserMissing() {
        TwoFactorVerifyDto dto = verifyDto();
        when(twoFactorAuthService.verifyLogin(dto)).thenReturn(payload());
        when(ssoUserService.getById(USER_ID)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> authService.verifyTwoFactorLogin(dto));
        assertEquals("账号不存在或已被删除", ex.getMessage());
        // 门禁拒绝时不得创建会话、不发登录事件
        stpMock.verify(() -> StpUtil.login(anyLong(), any(SaLoginParameter.class)), never());
        springMock.verify(() -> SpringUtil.publishEvent(any()), never());
    }

    @Test
    void verifyTwoFactorLogin_shouldThrow_whenUserDisabled() {
        User disabled = user();
        disabled.setState(false);
        TwoFactorVerifyDto dto = verifyDto();
        when(twoFactorAuthService.verifyLogin(dto)).thenReturn(payload());
        when(ssoUserService.getById(USER_ID)).thenReturn(disabled);

        ArgumentException ex = assertThrows(ArgumentException.class,
                () -> authService.verifyTwoFactorLogin(dto));
        assertEquals("账号已被禁用，请联系管理员", ex.getMessage());
        stpMock.verify(() -> StpUtil.login(anyLong(), any(SaLoginParameter.class)), never());
        springMock.verify(() -> SpringUtil.publishEvent(any()), never());
    }

    @Test
    void verifyTwoFactorLogin_shouldIssueToken_whenUserActive() {
        TwoFactorVerifyDto dto = verifyDto();
        when(twoFactorAuthService.verifyLogin(dto)).thenReturn(payload());
        when(ssoUserService.getById(USER_ID)).thenReturn(user());

        // loginAuthenticated 真实执行所需的 Sa-Token 静态桩
        SaSession session = mock(SaSession.class);
        stpMock.when(StpUtil::getTokenSession).thenReturn(session);
        SaTokenInfo tokenInfo = new SaTokenInfo();
        tokenInfo.setTokenTimeout(7200L);
        stpMock.when(StpUtil::getTokenInfo).thenReturn(tokenInfo);
        when(saTokenConfig.getTimeout()).thenReturn(3600L);
        saTempMock.when(() -> SaTempUtil.createToken(anyString(), anyLong()))
                .thenReturn("refresh-token");
        stubEmptyOrg();

        R<LoginVo> result = authService.verifyTwoFactorLogin(dto);

        assertEquals("TOKEN", result.getData().getStatus());
        stpMock.verify(() -> StpUtil.login(eq(USER_ID), any(SaLoginParameter.class)));
    }

    private TwoFactorTicketPayload payload() {
        TwoFactorTicketPayload payload = new TwoFactorTicketPayload();
        payload.setUserId(USER_ID);
        payload.setAuthType(AuthTypeEnum.CAPTCHA.name());
        payload.setAccount("admin");
        payload.setDeviceId("verify-device-id");
        payload.setDeviceInfo("verify-device-info");
        return payload;
    }

    private TwoFactorVerifyDto verifyDto() {
        TwoFactorVerifyDto dto = new TwoFactorVerifyDto();
        dto.setTicket("ticket");
        dto.setCode("123456");
        return dto;
    }

    private void stubEmptyOrg() {
        when(ssoUserService.findOrgIdByUserId(USER_ID)).thenReturn(List.of());
        when(ssoUserService.findDeptByUserId(USER_ID, null)).thenReturn(List.of());
        when(ssoUserService.getDefaultOrg(any(), isNull())).thenReturn(null);
        when(ssoUserService.findCompanyByUserId(USER_ID)).thenReturn(List.of());
        when(ssoUserService.updateById(any())).thenReturn(true);
    }

    private User user() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername("zhangsan");
        return user;
    }

    private LoginDto loginDto(AuthTypeEnum authType, String username,
                              String deviceId, String deviceInfo) {
        LoginDto login = new LoginDto();
        login.setAuthType(authType);
        login.setUsername(username);
        login.setDeviceId(deviceId);
        login.setDeviceInfo(deviceInfo);
        return login;
    }
}
