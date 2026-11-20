package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.exception.BizException;
import top.mddata.workbench.dto.FingerprintLoginVerifyDto;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.SocialBindExistingDto;
import top.mddata.workbench.dto.SocialCallbackDto;
import top.mddata.workbench.dto.TwoFactorSendDto;
import top.mddata.workbench.dto.TwoFactorVerifyDto;
import top.mddata.workbench.dto.WxMaLoginDto;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.auth.AuthService;
import top.mddata.workbench.service.fingerprint.FingerprintService;
import top.mddata.workbench.service.social.SocialLoginService;
import top.mddata.workbench.service.social.SocialOnboardingService;
import top.mddata.workbench.service.twofactor.TwoFactorAuthService;
import top.mddata.workbench.vo.FingerprintLoginOptionsVo;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.SocialLoginResultVo;
import top.mddata.workbench.vo.SocialProviderVo;

import java.util.List;


/**
 * 登录页业务接口，覆盖账号、微信、社交、指纹及双因素登录。
 *
 * @author henhen6
 * @since 2025年11月13日11:38:15
 */
@RestController
@AllArgsConstructor
@RequestMapping("/anyUser")
@Tag(name = "登录", description = "登录页业务认证接口")
public class LoginController {

    private final AuthService authService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final SocialLoginService socialLoginService;
    private final SocialOnboardingService socialOnboardingService;
    private final FingerprintService fingerprintService;

    @Operation(summary = "WEB端-登录", description = "单点登录页面登录时使用")
    @PostMapping("/auth/login")
    public R<LoginVo> login(@Validated @RequestBody LoginDto login) throws BizException {
        return authService.login(login);
    }

    @Operation(summary = "微信小程序端-一键登录", description = "loginCode 换 openid，phoneCode 换手机号，匹配不到用户时自动注册")
    @PostMapping("/auth/wxMa/login")
    @RequestLog(value = "微信小程序一键登录", logType = RequestLog.LogType.OTHER, request = false)
    public R<LoginVo> loginByWxMa(@Validated @RequestBody WxMaLoginDto login) {
        return authService.loginByWxMa(login);
    }

    @Operation(summary = "双因素认证-发送登录二次验证码", description = "凭登录第一步返回的票据发送 OTP")
    @PostMapping("/auth/twoFactor/send")
    public R<String> send(@Validated @RequestBody TwoFactorSendDto dto) {
        return R.success(twoFactorAuthService.sendLoginOtp(dto));
    }

    @Operation(summary = "双因素认证-校验登录二次验证码", description = "验证通过后签发登录令牌")
    @PostMapping("/auth/twoFactor/verify")
    @RequestLog(value = "双因素登录二次验证", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<LoginVo> verify(@Validated @RequestBody TwoFactorVerifyDto dto) {
        // 票据消费、账号存在性/状态复核与会话颁发全部在服务层完成
        return authService.verifyTwoFactorLogin(dto);
    }

    @Operation(summary = "Oauth2认证-获取已启用的第三方登录平台", description = "前端只渲染已启用平台入口")
    @GetMapping("/social/providers")
    @RequestLog(value = "查询第三方登录平台", logType = RequestLog.LogType.QUERY)
    public R<List<SocialProviderVo>> providers() {
        return R.success(socialLoginService.providers());
    }

    @Operation(summary = "Oauth2认证-获取第三方授权地址", description = "创建登录用一次性 state 并返回官方授权地址")
    @GetMapping("/social/{provider}/loginUrl")
    @RequestLog(value = "获取第三方授权地址", logType = RequestLog.LogType.QUERY, response = false)
    public R<String> loginUrl(@PathVariable String provider) {
        return R.success(socialLoginService.loginUrl(SocialProviderEnum.fromCode(provider)));
    }

    @Operation(summary = "Oauth2认证-第三方账号登录", description = "已绑定直接登录，未绑定返回待选择票据")
    @PostMapping("/social/login")
    @RequestLog(value = "第三方账号登录", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<SocialLoginResultVo> login(@Validated @RequestBody SocialCallbackDto callback) {
        return R.success(socialLoginService.login(callback));
    }

    @Operation(summary = "Oauth2认证-绑定已有账号", description = "验证已有MDP账号凭据后绑定第三方身份并登录")
    @PostMapping("/social/bindExisting")
    @RequestLog(value = "第三方身份绑定已有账号", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<LoginVo> bindExisting(@Validated @RequestBody SocialBindExistingDto dto) {
        return R.success(socialOnboardingService.bindExisting(dto));
    }

    @Operation(summary = "指纹登录-获取指纹登录配置", description = "免登录，生成指纹登录（WebAuthn 认证）配置")
    @GetMapping("/fingerprint/login/options")
    public R<FingerprintLoginOptionsVo> loginOptions() {
        return R.success(fingerprintService.loginOptions());
    }

    @Operation(summary = "指纹登录-验证", description = "免登录，校验 WebAuthn 认证响应并完成登录")
    @PostMapping("/fingerprint/login/verify")
    public R<LoginVo> loginVerify(@Validated @RequestBody FingerprintLoginVerifyDto dto) {
        return fingerprintService.loginVerify(dto);
    }
}
