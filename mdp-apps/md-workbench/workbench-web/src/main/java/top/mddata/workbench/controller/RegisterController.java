package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.exception.BizException;
import top.mddata.workbench.dto.RegisterByEmailDto;
import top.mddata.workbench.dto.RegisterByPhoneDto;
import top.mddata.workbench.dto.RegisterByUsernameDto;
import top.mddata.workbench.dto.SocialRegisterDto;
import top.mddata.workbench.service.auth.AuthService;
import top.mddata.workbench.service.social.SocialOnboardingService;
import top.mddata.workbench.service.user.SsoUserService;
import top.mddata.workbench.vo.LoginVo;

/**
 * 账号注册接口，覆盖普通注册与第三方身份创建新账号。
 *
 * @author henhen6
 */
@RestController
@AllArgsConstructor
@Tag(name = "注册", description = "普通账号与第三方身份注册接口")
public class RegisterController {

    private final AuthService authService;
    private final SsoUserService ssoUserService;
    private final SocialOnboardingService socialOnboardingService;

    @Operation(summary = "根据手机号注册", description = "根据手机号注册")
    @PostMapping("/anyUser/auth/registerByPhone")
    @RequestLog(value = "根据手机号注册", logType = RequestLog.LogType.ADD)
    public R<String> registerByPhone(@Validated @RequestBody RegisterByPhoneDto register) throws BizException {
        return R.success(authService.registerByPhone(register));
    }

    @Operation(summary = "根据邮箱注册", description = "根据邮箱注册")
    @PostMapping("/anyUser/auth/registerByEmail")
    @RequestLog(value = "根据邮箱注册", logType = RequestLog.LogType.ADD)
    public R<String> registerByEmail(@Validated @RequestBody RegisterByEmailDto register) throws BizException {
        return R.success(authService.registerByEmail(register));
    }

    @Operation(summary = "根据用户名注册", description = "根据用户名注册")
    @PostMapping("/anyUser/auth/registerByUsername")
    @RequestLog(value = "根据用户名注册", logType = RequestLog.LogType.ADD)
    public R<String> registerByUsername(@Validated @RequestBody RegisterByUsernameDto register) throws BizException {
        return R.success(authService.registerByUsername(register));
    }

    @Operation(summary = "检测手机号是否存在")
    @GetMapping("/anyUser/auth/checkPhone")
    @RequestLog(value = "检测手机号是否存在", logType = RequestLog.LogType.QUERY)
    public R<Boolean> checkPhone(@RequestParam String phone) {
        return R.success(ssoUserService.checkPhone(phone, null));
    }

    @Operation(summary = "创建新账号", description = "创建新MDP账号并绑定第三方身份后自动登录")
    @PostMapping("/anyUser/social/register")
    @RequestLog(value = "第三方身份创建新账号", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<LoginVo> registerBySocial(@Validated @RequestBody SocialRegisterDto dto) {
        return R.success(socialOnboardingService.register(dto));
    }
}
