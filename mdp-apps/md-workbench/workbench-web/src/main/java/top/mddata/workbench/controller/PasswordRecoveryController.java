package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.exception.BizException;
import top.mddata.workbench.dto.ForgetPasswordByEmailDto;
import top.mddata.workbench.service.auth.AuthService;

/**
 * 匿名密码找回与重置接口。
 *
 * @author henhen6
 */
@RestController
@RequestMapping("/anyUser/auth")
@AllArgsConstructor
@Tag(name = "密码找回", description = "忘记密码、校验重置凭证与重置密码")
public class PasswordRecoveryController {

    private final AuthService authService;

    @Operation(summary = "忘记密码-发邮件", description = "忘记密码-发邮件")
    @PostMapping("/forgetPassword")
    @RequestLog(value = "忘记密码-发邮件", logType = RequestLog.LogType.UPDATE)
    public R<Boolean> forgetPassword(@RequestParam @NotNull(message = "邮箱不能为空") String email)
            throws BizException {
        return R.success(authService.forgetPassword(email));
    }

    @Operation(summary = "忘记密码-检查token", description = "忘记密码-检查token")
    @PostMapping("/checkToken")
    @RequestLog(value = "忘记密码-检查token", logType = RequestLog.LogType.QUERY)
    public R<Boolean> checkToken(@RequestParam @NotNull(message = "token不能为空") String token)
            throws BizException {
        return R.success(authService.checkToken(token));
    }

    @Operation(summary = "忘记密码-重置密码", description = "忘记密码-重置密码")
    @PostMapping("/updateEmailByToken")
    @RequestLog(value = "忘记密码-重置密码", logType = RequestLog.LogType.UPDATE)
    public R<Boolean> updateEmailByToken(@Validated @RequestBody ForgetPasswordByEmailDto email)
            throws BizException {
        return R.success(authService.updateEmailByToken(email));
    }
}
