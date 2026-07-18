package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.exception.BizException;
import top.mddata.workbench.dto.ForgetPasswordByEmailDto;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.RegisterByEmailDto;
import top.mddata.workbench.dto.RegisterByPhoneDto;
import top.mddata.workbench.dto.RegisterByUsernameDto;
import top.mddata.workbench.service.AuthService;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.vo.LoginVo;


/**
 * 登录页接口
 *
 * @author henhen6
 * @since 2025年11月13日11:38:15
 */
@Slf4j
@RestController
@RequestMapping("/anyUser/auth")
@AllArgsConstructor
@Tag(name = "登录-注册", description = "认证接口")
public class AuthController {
    private final AuthService authService;
    private final SsoUserService ssoUserService;

    @Operation(summary = "登录", description = "单点登录页面登录时使用")
    @PostMapping(value = "/login")
    public R<LoginVo> login(@Validated @RequestBody LoginDto login) throws BizException {
        return authService.login(login);
    }

    @Operation(summary = "根据手机号注册", description = "根据手机号注册")
    @PostMapping(value = "/registerByPhone")
    @RequestLog(value = "根据手机号注册", logType = RequestLog.LogType.ADD)
    public R<String> registerByPhone(@Validated @RequestBody RegisterByPhoneDto register) throws BizException {
        return R.success(authService.registerByPhone(register));
    }

    @Operation(summary = "根据邮箱注册", description = "根据邮箱注册")
    @PostMapping(value = "/registerByEmail")
    @RequestLog(value = "根据邮箱注册", logType = RequestLog.LogType.ADD)
    public R<String> registerByEmail(@Validated @RequestBody RegisterByEmailDto register) throws BizException {
        return R.success(authService.registerByEmail(register));
    }

    @Operation(summary = "根据用户名注册", description = "根据用户名注册")
    @PostMapping(value = "/registerByUsername")
    @RequestLog(value = "根据用户名注册", logType = RequestLog.LogType.ADD)
    public R<String> registerByUsername(@Validated @RequestBody RegisterByUsernameDto register) throws BizException {
        return R.success(authService.registerByUsername(register));
    }

    @Operation(summary = "忘记密码-发邮件", description = "忘记密码-发邮件")
    @PostMapping(value = "/forgetPassword")
    @RequestLog(value = "忘记密码-发邮件", logType = RequestLog.LogType.UPDATE)
    public R<Boolean> forgetPassword(@RequestParam @NotNull(message = "邮箱不能为空") String email) throws BizException {
        return R.success(authService.forgetPassword(email));
    }

    @Operation(summary = "忘记密码-检查token", description = "忘记密码-检查token")
    @PostMapping(value = "/checkToken")
    @RequestLog(value = "忘记密码-检查token", logType = RequestLog.LogType.QUERY)
    public R<Boolean> checkToken(@RequestParam @NotNull(message = "token不能为空") String token) throws BizException {
        return R.success(authService.checkToken(token));
    }

    @Operation(summary = "忘记密码-重置密码", description = "忘记密码-重置密码")
    @PostMapping(value = "/updateEmailByToken")
    @RequestLog(value = "忘记密码-重置密码", logType = RequestLog.LogType.UPDATE)
    public R<Boolean> updateEmailByToken(@Validated @RequestBody ForgetPasswordByEmailDto email) throws BizException {
        return R.success(authService.updateEmailByToken(email));
    }

    @Operation(summary = "检测手机号是否存在")
    @GetMapping("/checkPhone")
    @RequestLog(value = "检测手机号是否存在", logType = RequestLog.LogType.QUERY)
    public R<Boolean> checkPhone(@RequestParam String phone) {
        return R.success(ssoUserService.checkPhone(phone, null));
    }

}
