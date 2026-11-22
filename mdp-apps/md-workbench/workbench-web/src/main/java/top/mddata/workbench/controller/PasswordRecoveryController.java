package top.mddata.workbench.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.anji.captcha.model.common.RepCodeEnum;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
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
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.workbench.dto.ForgetPasswordByCodeDto;
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
    private final CaptchaService captchaService;

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

    @Operation(summary = "忘记密码-发送验证码", description = "手机号走短信、邮箱走邮件，需先通过行为验证码")
    @GetMapping("/forgetPassword/sendCode")
    @RequestLog(value = "忘记密码-发送验证码", logType = RequestLog.LogType.OTHER)
    public R<String> sendForgetPasswordCode(
            @RequestParam @NotBlank(message = "请填写手机号或邮箱") String account,
            CaptchaVO captchaReq, HttpServletRequest request) {
        // 行为验证码校验与 CaptchaController 发码端点同构，防匿名短信/邮件轰炸
        captchaReq.setBrowserInfo(
                JakartaServletUtil.getClientIP(request) + request.getHeader(HttpHeaders.USER_AGENT));
        ResponseModel verificationRes = captchaService.verification(captchaReq);
        ArgumentAssert.equals(
                verificationRes.getRepCode(), RepCodeEnum.SUCCESS.getCode(), verificationRes.getRepMsg());
        return R.success(authService.sendForgetPasswordCode(account));
    }

    @Operation(summary = "忘记密码-凭验证码重置密码", description = "校验找回场景验证码后重置密码")
    @PostMapping("/forgetPassword/resetByCode")
    @RequestLog(value = "忘记密码-凭验证码重置密码", logType = RequestLog.LogType.UPDATE)
    public R<Boolean> resetPasswordByCode(@Validated @RequestBody ForgetPasswordByCodeDto dto)
            throws BizException {
        return R.success(authService.resetPasswordByCode(dto));
    }
}
