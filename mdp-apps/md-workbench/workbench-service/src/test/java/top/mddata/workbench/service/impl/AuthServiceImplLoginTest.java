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
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import top.mddata.base.base.R;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.common.constant.DefValConstants;
import top.mddata.common.entity.User;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.facade.message.MsgFacade;
import top.mddata.console.facade.organization.UserFacade;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.event.LoginEvent;
import top.mddata.workbench.service.AccountAuthenticator;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.service.WxMaAuthService;
import top.mddata.workbench.vo.LoginVo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthServiceImpl} 普通登录与会话签发单测。
 *
 * <p>验证：login 委托 {@link AccountAuthenticator} 完成认证后，以 PC 设备调用统一会话签发；
 * loginAuthenticated 的 Sa-Token 登录参数、TokenSession、LoginVo 字段与登录日志事件不回归。</p>
 *
 * @author tangyh
 */
class AuthServiceImplLoginTest {

    private static final Long USER_ID = 1L;

    private SaTokenConfig saTokenConfig;
    private SsoUserService ssoUserService;
    private AccountAuthenticator accountAuthenticator;
    private AuthServiceImpl authService;

    private MockedStatic<StpUtil> stpMock;
    private MockedStatic<SaTempUtil> saTempMock;
    private MockedStatic<SpringUtil> springMock;

    @BeforeEach
    void setUp() {
        saTokenConfig = mock(SaTokenConfig.class);
        ssoUserService = mock(SsoUserService.class);
        accountAuthenticator = mock(AccountAuthenticator.class);
        authService = new AuthServiceImpl(mock(SystemProperties.class), saTokenConfig,
                mock(CacheOps.class), ssoUserService, mock(UserFacade.class), mock(MsgFacade.class),
                mock(WxMaAuthService.class), accountAuthenticator);

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
    void login委托认证组件并以PC设备签发会话() {
        AuthServiceImpl spy = spy(authService);
        User user = user();
        LoginVo loginVo = new LoginVo();
        LoginDto login = loginDto();
        when(accountAuthenticator.authenticate(login)).thenReturn(user);
        doReturn(loginVo).when(spy).loginAuthenticated(user, AuthTypeEnum.USERNAME,
                "zhangsan", "device-id", "device-info", "PC");

        R<LoginVo> result = spy.login(login);

        assertSame(loginVo, result.getData());
        verify(spy).loginAuthenticated(user, AuthTypeEnum.USERNAME,
                "zhangsan", "device-id", "device-info", "PC");
    }

    @Test
    void loginAuthenticated签发会话写入TokenSession并记录登录日志() {
        User user = user();
        stubTokenSession();
        SaTokenInfo tokenInfo = new SaTokenInfo();
        tokenInfo.setTokenName("satoken");
        tokenInfo.setTokenValue("token-value");
        tokenInfo.setTokenTimeout(7200L);
        stpMock.when(StpUtil::getTokenInfo).thenReturn(tokenInfo);
        when(saTokenConfig.getTimeout()).thenReturn(3600L);
        saTempMock.when(() -> SaTempUtil.createToken(anyString(), eq(7200L)))
                .thenReturn("refresh-token");
        stubEmptyOrg();

        LoginVo result = authService.loginAuthenticated(user, AuthTypeEnum.WECHAT_OPEN,
                "zhangsan", "device-id", "device-info", "PC");

        // Sa-Token 登录参数：设备类型与设备 id 透传
        ArgumentCaptor<SaLoginParameter> loginParamCaptor =
                ArgumentCaptor.forClass(SaLoginParameter.class);
        stpMock.verify(() -> StpUtil.login(eq(USER_ID), loginParamCaptor.capture()));
        assertEquals("PC", loginParamCaptor.getValue().getDeviceType());
        assertEquals("device-id", loginParamCaptor.getValue().getDeviceId());

        // TokenSession 写入登录用户 id，组织字段按空组织结果清理
        SaSession session = stpTokenSession();
        verify(session).setLoginId(USER_ID);

        // 返回字段与重构前一致
        assertEquals(USER_ID, result.getId());
        assertEquals("satoken", result.getTokenName());
        assertEquals("token-value", result.getTokenValue());
        assertEquals(7200L, result.getExpire());
        assertEquals("refresh-token", result.getRefreshToken());

        // 登录日志：社交类型、账号、设备信息与默认工作台应用
        LoginLogDto logDto = captureLoginEvent();
        assertEquals(AuthTypeEnum.WECHAT_OPEN, logDto.getAuthType());
        assertEquals("zhangsan", logDto.getAccount());
        assertEquals("device-info", logDto.getDeviceInfo());
        assertEquals(DefValConstants.WORKBENCH_APP_KEY, logDto.getAppKey());
        assertEquals(DefValConstants.WORKBENCH_APP_NAME, logDto.getAppName());
        assertNotNull(logDto.getTokenInfo());
    }

    @Test
    void loginAuthenticated在deviceId为空时生成随机设备id() {
        stubTokenSession();
        SaTokenInfo tokenInfo = new SaTokenInfo();
        tokenInfo.setTokenTimeout(100L);
        stpMock.when(StpUtil::getTokenInfo).thenReturn(tokenInfo);
        when(saTokenConfig.getTimeout()).thenReturn(3600L);
        saTempMock.when(() -> SaTempUtil.createToken(anyString(), anyLong()))
                .thenReturn("refresh-token");
        stubEmptyOrg();

        authService.loginAuthenticated(user(), AuthTypeEnum.USERNAME,
                "zhangsan", "", null, "PC");

        ArgumentCaptor<SaLoginParameter> loginParamCaptor =
                ArgumentCaptor.forClass(SaLoginParameter.class);
        stpMock.verify(() -> StpUtil.login(eq(USER_ID), loginParamCaptor.capture()));
        String deviceId = loginParamCaptor.getValue().getDeviceId();
        assertNotNull(deviceId);
        assertFalse(deviceId.isBlank());
    }

    private void stubTokenSession() {
        SaSession session = mock(SaSession.class);
        stpMock.when(StpUtil::getTokenSession).thenReturn(session);
    }

    private SaSession stpTokenSession() {
        return StpUtil.getTokenSession();
    }

    private void stubEmptyOrg() {
        when(ssoUserService.findOrgIdByUserId(USER_ID)).thenReturn(List.of());
        when(ssoUserService.findDeptByUserId(USER_ID, null)).thenReturn(List.of());
        when(ssoUserService.getDefaultOrg(any(), isNull())).thenReturn(null);
        when(ssoUserService.findCompanyByUserId(USER_ID)).thenReturn(List.of());
        when(ssoUserService.updateById(any())).thenReturn(true);
    }

    private LoginLogDto captureLoginEvent() {
        ArgumentCaptor<LoginEvent> eventCaptor = ArgumentCaptor.forClass(LoginEvent.class);
        springMock.verify(() -> SpringUtil.publishEvent(eventCaptor.capture()));
        return (LoginLogDto) eventCaptor.getValue().getSource();
    }

    private User user() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername("zhangsan");
        return user;
    }

    private LoginDto loginDto() {
        LoginDto login = new LoginDto();
        login.setAuthType(AuthTypeEnum.USERNAME);
        login.setUsername("zhangsan");
        login.setPassword("password");
        login.setDeviceId("device-id");
        login.setDeviceInfo("device-info");
        return login;
    }
}
