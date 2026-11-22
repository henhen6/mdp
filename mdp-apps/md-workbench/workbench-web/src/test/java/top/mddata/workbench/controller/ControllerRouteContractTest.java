package top.mddata.workbench.controller;

import com.anji.captcha.model.vo.CaptchaVO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import top.mddata.common.dto.IdDto;
import top.mddata.workbench.controller.security.AccountSecurityController;
import top.mddata.workbench.controller.security.FingerprintSecurityController;
import top.mddata.workbench.controller.security.SocialSecurityController;
import top.mddata.workbench.controller.security.TwoFactorSecurityController;
import top.mddata.workbench.dto.ForgetPasswordByCodeDto;
import top.mddata.workbench.dto.ForgetPasswordByEmailDto;
import top.mddata.workbench.dto.FingerprintLoginVerifyDto;
import top.mddata.workbench.dto.FingerprintRegisterVerifyDto;
import top.mddata.workbench.dto.FingerprintRenameDto;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.ProfileEmailDto;
import top.mddata.workbench.dto.ProfilePasswordDto;
import top.mddata.workbench.dto.ProfilePhoneDto;
import top.mddata.workbench.dto.ProfileUserDto;
import top.mddata.workbench.dto.ProfileUsernameDto;
import top.mddata.workbench.dto.RegisterByEmailDto;
import top.mddata.workbench.dto.RegisterByPhoneDto;
import top.mddata.workbench.dto.RegisterByUsernameDto;
import top.mddata.workbench.dto.SocialBindExistingDto;
import top.mddata.workbench.dto.SocialCallbackDto;
import top.mddata.workbench.dto.SocialReauthDto;
import top.mddata.workbench.dto.SocialReauthUrlDto;
import top.mddata.workbench.dto.SocialRegisterDto;
import top.mddata.workbench.dto.SocialTicketDto;
import top.mddata.workbench.dto.SocialTransferDto;
import top.mddata.workbench.dto.SocialUnbindDto;
import top.mddata.workbench.dto.TwoFactorChannelDto;
import top.mddata.workbench.dto.TwoFactorDisableDto;
import top.mddata.workbench.dto.TwoFactorEnableDto;
import top.mddata.workbench.dto.TwoFactorSendDto;
import top.mddata.workbench.dto.TwoFactorVerifyDto;
import top.mddata.workbench.dto.WxMaLoginDto;
import top.mddata.workbench.dto.WxMaPhoneBindDto;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 登录、注册与安全设置拆分后的路由契约，防止移动方法时改变前端请求地址。
 *
 * @author tangyh
 */
class ControllerRouteContractTest {

    @Test
    void 登录接口统一归属登录控制器() throws Exception {
        assertPost(LoginController.class, "login", "/anyUser/auth/login", LoginDto.class);
        assertPost(LoginController.class, "loginByWxMa", "/anyUser/auth/wxMa/login", WxMaLoginDto.class);
        assertPost(LoginController.class, "send", "/anyUser/auth/twoFactor/send", TwoFactorSendDto.class);
        assertPost(LoginController.class, "verify", "/anyUser/auth/twoFactor/verify", TwoFactorVerifyDto.class);
        assertGet(LoginController.class, "providers", "/anyUser/social/providers");
        assertGet(LoginController.class, "loginUrl", "/anyUser/social/{provider}/loginUrl", String.class);
        assertPost(LoginController.class, "login", "/anyUser/social/login", SocialCallbackDto.class);
        assertPost(LoginController.class, "bindExisting", "/anyUser/social/bindExisting", SocialBindExistingDto.class);
        assertGet(LoginController.class, "loginOptions", "/anyUser/fingerprint/login/options");
        assertPost(LoginController.class, "loginVerify", "/anyUser/fingerprint/login/verify",
                FingerprintLoginVerifyDto.class);
    }

    @Test
    void 注册与密码找回接口分别归属独立控制器() throws Exception {
        assertPost(RegisterController.class, "registerByPhone", "/anyUser/auth/registerByPhone",
                RegisterByPhoneDto.class);
        assertPost(RegisterController.class, "registerByEmail", "/anyUser/auth/registerByEmail",
                RegisterByEmailDto.class);
        assertPost(RegisterController.class, "registerByUsername", "/anyUser/auth/registerByUsername",
                RegisterByUsernameDto.class);
        assertGet(RegisterController.class, "checkPhone", "/anyUser/auth/checkPhone", String.class);
        assertPost(RegisterController.class, "registerBySocial", "/anyUser/social/register",
                SocialRegisterDto.class);

        assertPost(PasswordRecoveryController.class, "forgetPassword", "/anyUser/auth/forgetPassword", String.class);
        assertPost(PasswordRecoveryController.class, "checkToken", "/anyUser/auth/checkToken", String.class);
        assertPost(PasswordRecoveryController.class, "updateEmailByToken", "/anyUser/auth/updateEmailByToken",
                ForgetPasswordByEmailDto.class);
        assertGet(PasswordRecoveryController.class, "sendForgetPasswordCode",
                "/anyUser/auth/forgetPassword/sendCode",
                String.class, CaptchaVO.class, HttpServletRequest.class);
        assertPost(PasswordRecoveryController.class, "resetPasswordByCode",
                "/anyUser/auth/forgetPassword/resetByCode", ForgetPasswordByCodeDto.class);
    }

    @Test
    void 账号安全接口保持个人中心路径() throws Exception {
        assertPost(ProfileController.class, "updateProfile", "/profile/updateProfile", ProfileUserDto.class);
        assertPost(AccountSecurityController.class, "updatePhone", "/profile/updatePhone", ProfilePhoneDto.class);
        assertPost(AccountSecurityController.class, "updatePhoneByWxMa", "/profile/updatePhoneByWxMa",
                WxMaPhoneBindDto.class);
        assertPost(AccountSecurityController.class, "updateEmail", "/profile/updateEmail", ProfileEmailDto.class);
        assertPost(AccountSecurityController.class, "updatePassword", "/profile/updatePassword",
                ProfilePasswordDto.class);
        assertPost(AccountSecurityController.class, "updateUsername", "/profile/updateUsername",
                ProfileUsernameDto.class);
        assertGet(AccountSecurityController.class, "sendPhoneCode", "/profile/sendPhoneCode",
                String.class, String.class);
        assertGet(AccountSecurityController.class, "sendEmailCode", "/profile/sendEmailCode",
                String.class, String.class);
    }

    @Test
    void 社交与双因素安全接口保持原路径() throws Exception {
        assertGet(SocialSecurityController.class, "list", "/profile/social/list");
        assertPost(SocialSecurityController.class, "reauth", "/profile/social/reauth", SocialReauthDto.class);
        assertPost(SocialSecurityController.class, "reauthUrl", "/profile/social/{provider}/reauthUrl",
                String.class, SocialReauthUrlDto.class);
        assertPost(SocialSecurityController.class, "reauthByCallback", "/profile/social/reauthByCallback",
                SocialCallbackDto.class);
        assertPost(SocialSecurityController.class, "bindUrl", "/profile/social/{provider}/bindUrl",
                String.class, SocialTicketDto.class);
        assertPost(SocialSecurityController.class, "bind", "/profile/social/bind", SocialCallbackDto.class);
        assertPost(SocialSecurityController.class, "unbind", "/profile/social/unbind", SocialUnbindDto.class);
        assertPost(SocialSecurityController.class, "transfer", "/profile/social/transfer", SocialTransferDto.class);

        assertGet(TwoFactorSecurityController.class, "status", "/profile/twoFactor/status");
        assertPost(TwoFactorSecurityController.class, "sendEnableCode", "/profile/twoFactor/sendEnableCode",
                TwoFactorChannelDto.class);
        assertPost(TwoFactorSecurityController.class, "enable", "/profile/twoFactor/enable",
                TwoFactorEnableDto.class);
        assertPost(TwoFactorSecurityController.class, "disable", "/profile/twoFactor/disable",
                TwoFactorDisableDto.class);
        assertPost(TwoFactorSecurityController.class, "changeChannel", "/profile/twoFactor/changeChannel",
                TwoFactorEnableDto.class);
    }

    @Test
    void 指纹安全接口保持原路径() throws Exception {
        assertPost(FingerprintSecurityController.class, "checkPassword", "/profile/fingerprint/checkPassword",
                FingerprintSecurityController.CheckPasswordDto.class);
        assertPost(FingerprintSecurityController.class, "registrationOptions",
                "/profile/fingerprint/registration/options");
        assertPost(FingerprintSecurityController.class, "registrationVerify",
                "/profile/fingerprint/registration/verify", FingerprintRegisterVerifyDto.class);
        assertGet(FingerprintSecurityController.class, "list", "/profile/fingerprint/list");
        assertPost(FingerprintSecurityController.class, "rename", "/profile/fingerprint/rename",
                FingerprintRenameDto.class);
        assertPost(FingerprintSecurityController.class, "delete", "/profile/fingerprint/delete", IdDto.class);
    }

    private void assertPost(Class<?> controller, String methodName, String expectedPath,
                            Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = controller.getDeclaredMethod(methodName, parameterTypes);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertNotNull(mapping, () -> controller.getSimpleName() + "." + methodName + " 缺少 @PostMapping");
        assertPath(controller, methodName, expectedPath, mapping.value());
    }

    private void assertGet(Class<?> controller, String methodName, String expectedPath,
                           Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = controller.getDeclaredMethod(methodName, parameterTypes);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertNotNull(mapping, () -> controller.getSimpleName() + "." + methodName + " 缺少 @GetMapping");
        assertPath(controller, methodName, expectedPath, mapping.value());
    }

    private void assertPath(Class<?> controller, String methodName, String expectedPath, String[] actualPaths) {
        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        String prefix = classMapping == null || classMapping.value().length == 0 ? "" : classMapping.value()[0];
        assertEquals(1, actualPaths.length, "每个接口必须且只能声明一个路径");
        assertEquals(expectedPath, prefix + actualPaths[0],
                () -> controller.getSimpleName() + "." + methodName + " 路由发生变化");
    }
}
