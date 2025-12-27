package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.base.exception.BizException;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.RegisterByEmailDto;
import top.mddata.workbench.dto.RegisterByPhoneDto;
import top.mddata.workbench.service.AuthService;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.vo.CaptchaVo;
import top.mddata.workbench.vo.LoginVo;

import static top.mddata.common.constant.SwaggerConstants.DATA_TYPE_STRING;


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

    /**
     * 注册
     */
    @Operation(summary = "根据手机号注册", description = "根据手机号注册")
    @PostMapping(value = "/registerByPhone")
    public R<String> registerByPhone(@Validated @RequestBody RegisterByPhoneDto register) throws BizException {
        return R.success(authService.registerByPhone(register));
    }

    @Operation(summary = "根据邮箱注册", description = "根据邮箱注册")
    @PostMapping(value = "/registerByEmail")
    public R<String> registerByEmail(@Validated @RequestBody RegisterByEmailDto register) throws BizException {
        return R.success(authService.registerByEmail(register));
    }

    @Operation(summary = "检测手机号是否存在")
    @GetMapping("/checkPhone")
    public R<Boolean> checkPhone(@RequestParam String phone) {
        return R.success(ssoUserService.checkPhone(phone, null));
    }


    @Operation(summary = "发送短信验证码", description = "发送短信验证码")
    @Parameters({
            @Parameter(name = "phone", description = "手机号", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
            @Parameter(name = "templateCode", description = "模板编码", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @GetMapping(value = "/sendPhoneCode")
    public R<String> sendPhoneCode(@RequestParam(value = "phone") String phone,
                                   @RequestParam(value = "templateCode") String templateCode) {
        return authService.sendPhoneCode(phone, templateCode);
    }

    @Parameters({
            @Parameter(name = "email", description = "邮箱", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
            @Parameter(name = "templateCode", description = "模板编码", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @Operation(summary = "发送邮箱验证码", description = "发送邮箱验证码")
    @GetMapping(value = "/sendEmailCode")
    public R<String> sendEmailCode(@RequestParam(value = "email") String email, @RequestParam(value = "templateCode") String templateCode) {
        return authService.sendEmailCode(email, templateCode);
    }

    @Operation(summary = "获取图片验证码", description = "获取图片验证码")
    @GetMapping(value = "/captcha")
    public R<CaptchaVo> captcha() {
        return R.success(authService.createImg());
    }
}
