package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.util.ContextUtil;
import top.mddata.workbench.dto.TwoFactorChannelDto;
import top.mddata.workbench.dto.TwoFactorDisableDto;
import top.mddata.workbench.dto.TwoFactorEnableDto;
import top.mddata.workbench.dto.TwoFactorSendDto;
import top.mddata.workbench.dto.TwoFactorVerifyDto;
import top.mddata.workbench.service.AuthService;
import top.mddata.workbench.service.TwoFactorAuthService;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.TwoFactorManageSendVo;
import top.mddata.workbench.vo.TwoFactorStatusVo;

/**
 * 双因素认证接口。
 *
 * <p>匿名段供登录第二步使用（凭一次性票据），需登录段供安全设置使用。
 * 两个前缀无法共用类级 {@code @RequestMapping}，故路径写在方法上。</p>
 *
 * @author tangyh
 * @since 2026-11-16
 */
@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "双因素认证", description = "登录二次验证与安全设置")
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;
    private final AuthService authService;

    // ==================== 匿名段：登录第二步 ====================

    @Operation(summary = "发送登录二次验证码", description = "凭登录第一步返回的票据发送 OTP")
    @PostMapping("/anyUser/auth/twoFactor/send")
    public R<String> send(@Validated @RequestBody TwoFactorSendDto dto) {
        return R.success(twoFactorAuthService.sendLoginOtp(dto));
    }

    @Operation(summary = "校验登录二次验证码", description = "验证通过后签发登录令牌")
    @PostMapping("/anyUser/auth/twoFactor/verify")
    @RequestLog(value = "双因素登录二次验证", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<LoginVo> verify(@Validated @RequestBody TwoFactorVerifyDto dto) {
        // 票据消费、账号存在性/状态复核与会话颁发全部在服务层完成
        return authService.verifyTwoFactorLogin(dto);
    }

    // ==================== 需登录段：安全设置 ====================

    @Operation(summary = "查询双因素认证状态")
    @GetMapping("/profile/twoFactor/status")
    public R<TwoFactorStatusVo> status() {
        return R.success(twoFactorAuthService.status(ContextUtil.getUserId()));
    }

    @Operation(summary = "发送开通/换渠道验证码")
    @PostMapping("/profile/twoFactor/sendEnableCode")
    public R<TwoFactorManageSendVo> sendEnableCode(@Validated @RequestBody TwoFactorChannelDto dto) {
        return R.success(twoFactorAuthService.sendManageCode(
                ContextUtil.getUserId(), dto.getChannel()));
    }

    @Operation(summary = "开通双因素认证")
    @PostMapping("/profile/twoFactor/enable")
    @RequestLog(value = "开通双因素认证", logType = RequestLog.LogType.UPDATE,
            request = false, response = false, requestByError = false)
    public R<Boolean> enable(@Validated @RequestBody TwoFactorEnableDto dto) {
        twoFactorAuthService.enable(ContextUtil.getUserId(), dto);
        return R.success();
    }

    @Operation(summary = "关闭双因素认证")
    @PostMapping("/profile/twoFactor/disable")
    @RequestLog(value = "关闭双因素认证", logType = RequestLog.LogType.UPDATE,
            request = false, response = false, requestByError = false)
    public R<Boolean> disable(@Validated @RequestBody TwoFactorDisableDto dto) {
        twoFactorAuthService.disable(ContextUtil.getUserId(), dto);
        return R.success();
    }

    @Operation(summary = "更换验证渠道")
    @PostMapping("/profile/twoFactor/changeChannel")
    @RequestLog(value = "更换双因素认证渠道", logType = RequestLog.LogType.UPDATE,
            request = false, response = false, requestByError = false)
    public R<Boolean> changeChannel(@Validated @RequestBody TwoFactorEnableDto dto) {
        twoFactorAuthService.changeChannel(ContextUtil.getUserId(), dto);
        return R.success();
    }
}
