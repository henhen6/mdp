package top.mddata.workbench.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.anji.captcha.model.common.RepCodeEnum;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.workbench.service.VerificationCodeService;
import top.mddata.workbench.vo.CaptchaVo;

import static top.mddata.common.constant.SwaggerConstants.DATA_TYPE_STRING;


/**
 * 登录页接口
 *
 * @author henhen6
 * @since 2025年11月13日11:38:15
 */
@Slf4j
@RestController
@RequestMapping("/anyUser/captcha")
@AllArgsConstructor
@Tag(name = "验证码接口", description = "验证码接口")
public class CaptchaController {
    private final VerificationCodeService verificationCodeService;


    @Operation(summary = "发送短信验证码", description = "发送短信验证码")
    @Parameters({
            @Parameter(name = "phone", description = "手机号", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
            @Parameter(name = "templateCode", description = "模板编码", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @GetMapping(value = "/send/phone")
    public R<String> sendPhoneCode(@RequestParam(value = "phone") String phone,
                                   @RequestParam(value = "templateCode") String templateCode) {
        return verificationCodeService.sendPhoneCode(phone, templateCode);
    }

    @Parameters({
            @Parameter(name = "email", description = "邮箱", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
            @Parameter(name = "templateCode", description = "模板编码", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @Operation(summary = "发送邮箱验证码", description = "发送邮箱验证码")
    @GetMapping(value = "/send/email")
    public R<String> sendEmailCode(@RequestParam(value = "email") String email, @RequestParam(value = "templateCode") String templateCode) {
        return verificationCodeService.sendEmailCode(email, templateCode);
    }

    @Operation(summary = "获取图片验证码", description = "获取图片验证码")
    @GetMapping(value = "/get/img")
    public R<CaptchaVo> captcha() {
        return R.success(verificationCodeService.createImg());
    }


    @Operation(summary = "获取行为验证码", description = "获取行为验证码")
    @GetMapping("/get/behavior")
    public R<Object> getBehaviorCaptcha(CaptchaVO captchaReq, HttpServletRequest request) {
        CaptchaService behaviorCaptchaService = SpringUtil.getBean(CaptchaService.class);
        captchaReq.setBrowserInfo(JakartaServletUtil.getClientIP(request) + request.getHeader(HttpHeaders.USER_AGENT));
        ResponseModel responseModel = behaviorCaptchaService.get(captchaReq);
        ArgumentAssert.equals(RepCodeEnum.SUCCESS.getCode(), responseModel.getRepCode(), responseModel.getRepMsg());
        return R.success(responseModel.getRepData());
    }

    @Operation(summary = "校验行为验证码", description = "校验行为验证码")
    @PostMapping("/check/behavior")
    public R<Object> checkBehaviorCaptcha(@RequestBody CaptchaVO captchaReq, HttpServletRequest request) {
        CaptchaService behaviorCaptchaService = SpringUtil.getBean(CaptchaService.class);
        captchaReq.setBrowserInfo(JakartaServletUtil.getClientIP(request) + request.getHeader(HttpHeaders.USER_AGENT));
        return R.success(behaviorCaptchaService.check(captchaReq));
    }
}
