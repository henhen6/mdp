package top.mddata.workbench.service.auth.impl;

import cn.dev33.satoken.config.SaTokenConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.ArgumentException;
import top.mddata.common.cache.workbench.TwoFactorCacheKeyBuilder;
import top.mddata.common.entity.User;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.facade.message.MsgFacade;
import top.mddata.console.facade.organization.UserFacade;
import top.mddata.workbench.dto.ForgetPasswordByCodeDto;
import top.mddata.workbench.service.auth.AccountAuthenticator;
import top.mddata.workbench.service.auth.WxMaAuthService;
import top.mddata.workbench.service.twofactor.TwoFactorAuthService;
import top.mddata.workbench.service.twofactor.TwoFactorOtpService;
import top.mddata.workbench.service.user.SsoUserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link AuthServiceImpl} 忘记密码验证码链路单测：发码的格式与账号存在性校验、
 * 重置时的一致性校验与 OTP 校验后按账号类型分流。
 *
 * @author tangyh
 */
class AuthServiceImplForgetPasswordTest {

    private SsoUserService ssoUserService;
    private TwoFactorOtpService twoFactorOtpService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ssoUserService = mock(SsoUserService.class);
        twoFactorOtpService = mock(TwoFactorOtpService.class);
        authService = new AuthServiceImpl(mock(SystemProperties.class), mock(SaTokenConfig.class),
                mock(CacheOps.class), ssoUserService, mock(UserFacade.class), mock(MsgFacade.class),
                mock(WxMaAuthService.class), mock(AccountAuthenticator.class),
                mock(TwoFactorAuthService.class), twoFactorOtpService);
    }

    private ForgetPasswordByCodeDto buildDto(String account, String code, String password,
                                             String confirmPassword) {
        return new ForgetPasswordByCodeDto()
                .setAccount(account)
                .setCode(code)
                .setPassword(password)
                .setConfirmPassword(confirmPassword);
    }

    @Test
    void 发码_账号格式非法时直接报错且不查询用户() {
        assertThrows(ArgumentException.class, () -> authService.sendForgetPasswordCode("abc123"));

        verifyNoInteractions(ssoUserService, twoFactorOtpService);
    }

    @Test
    void 发码_账号不存在时明确报错() {
        when(ssoUserService.getByPhone("13800000000")).thenReturn(null);

        assertThrows(ArgumentException.class, () -> authService.sendForgetPasswordCode("13800000000"));

        verifyNoInteractions(twoFactorOtpService);
    }

    @Test
    void 发码_手机号走手机查询并发送找回场景验证码() {
        when(ssoUserService.getByPhone("13800000000")).thenReturn(new User());
        when(twoFactorOtpService.send(TwoFactorCacheKeyBuilder.SCENE_FORGET_PASSWORD,
                "13800000000")).thenReturn("138****0000");

        String masked = authService.sendForgetPasswordCode("13800000000");

        assertEquals("138****0000", masked);
        verify(ssoUserService, never()).getByEmail(anyString());
    }

    @Test
    void 发码_邮箱走邮箱查询并发送找回场景验证码() {
        when(ssoUserService.getByEmail("a@example.com")).thenReturn(new User());
        when(twoFactorOtpService.send(TwoFactorCacheKeyBuilder.SCENE_FORGET_PASSWORD,
                "a@example.com")).thenReturn("a***@example.com");

        String masked = authService.sendForgetPasswordCode("a@example.com");

        assertEquals("a***@example.com", masked);
        verify(ssoUserService, never()).getByPhone(anyString());
    }

    @Test
    void 重置_两次密码不一致时直接报错且不校验验证码() {
        assertThrows(ArgumentException.class, () -> authService.resetPasswordByCode(
                buildDto("13800000000", "123456", "abc12345", "abc99999")));

        verifyNoInteractions(twoFactorOtpService, ssoUserService);
    }

    @Test
    void 重置_手机号账号经OTP校验后走手机重置() {
        when(ssoUserService.resetPwByPhone("13800000000", "abc12345")).thenReturn(true);

        Boolean result = authService.resetPasswordByCode(
                buildDto("13800000000", "123456", "abc12345", "abc12345"));

        assertTrue(result);
        verify(twoFactorOtpService).verify(
                eq(TwoFactorCacheKeyBuilder.SCENE_FORGET_PASSWORD), eq("13800000000"), eq("123456"));
        verify(ssoUserService, never()).resetPwByEmail(anyString(), anyString());
    }

    @Test
    void 重置_邮箱账号经OTP校验后走邮箱重置() {
        when(ssoUserService.resetPwByEmail("a@example.com", "abc12345")).thenReturn(true);

        Boolean result = authService.resetPasswordByCode(
                buildDto("a@example.com", "123456", "abc12345", "abc12345"));

        assertTrue(result);
        verify(twoFactorOtpService).verify(
                eq(TwoFactorCacheKeyBuilder.SCENE_FORGET_PASSWORD), eq("a@example.com"), eq("123456"));
        verify(ssoUserService, never()).resetPwByPhone(anyString(), anyString());
    }
}
