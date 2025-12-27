package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
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
@RequestMapping("/anyUser/verificationCode")
@AllArgsConstructor
@Tag(name = "验证码接口", description = "验证码接口")
public class VerificationCodeController {
    private final VerificationCodeService verificationCodeService;


    @Operation(summary = "发送短信验证码", description = "发送短信验证码")
    @Parameters({
            @Parameter(name = "phone", description = "手机号", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
            @Parameter(name = "templateCode", description = "模板编码", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @GetMapping(value = "/sendPhoneCode")
    public R<String> sendPhoneCode(@RequestParam(value = "phone") String phone,
                                   @RequestParam(value = "templateCode") String templateCode) {
        return verificationCodeService.sendPhoneCode(phone, templateCode);
    }

    @Parameters({
            @Parameter(name = "email", description = "邮箱", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
            @Parameter(name = "templateCode", description = "模板编码", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @Operation(summary = "发送邮箱验证码", description = "发送邮箱验证码")
    @GetMapping(value = "/sendEmailCode")
    public R<String> sendEmailCode(@RequestParam(value = "email") String email, @RequestParam(value = "templateCode") String templateCode) {
        return verificationCodeService.sendEmailCode(email, templateCode);
    }

    @Operation(summary = "获取图片验证码", description = "获取图片验证码")
    @GetMapping(value = "/captcha")
    public R<CaptchaVo> captcha() {
        return R.success(verificationCodeService.createImg());
    }
}
