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
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.workbench.dto.ProfileEmailDto;
import top.mddata.workbench.dto.ProfilePasswordDto;
import top.mddata.workbench.dto.ProfilePhoneDto;
import top.mddata.workbench.dto.ProfileUserDto;
import top.mddata.workbench.service.SsoUserService;

import static top.mddata.common.constant.SwaggerConstants.DATA_TYPE_STRING;

/**
 * 个人中心
 *
 * @author henhen6
 * @since 2025/7/1 18:52
 */
@Slf4j
@RestController
@RequestMapping("/profile")
@AllArgsConstructor
@Tag(name = "个人中心", description = "个人中心")
public class ProfileController {
    private final SsoUserService ssoUserService;

    /**
     * 修改个人信息
     *
     * @param dto 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/updateProfile")
    @Operation(summary = "修改个人信息", description = "修改个人信息")
    @RequestLog(value = "修改个人信息")
    public R<Long> updateProfile(@Validated @RequestBody ProfileUserDto dto) {
        return R.success(ssoUserService.updateProfile(dto));
    }


    /**
     * 修改个人手机号
     *
     * @param dto 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/updatePhone")
    @Operation(summary = "修改个人手机号", description = "修改个人手机号")
    @RequestLog(value = "修改个人手机号")
    public R<Long> updatePhone(@Validated @RequestBody ProfilePhoneDto dto) {
        return R.success(ssoUserService.updatePhone(dto));
    }

    /**
     * 修改个人邮箱
     *
     * @param dto 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/updateEmail")
    @Operation(summary = "修改个人邮箱", description = "修改个人邮箱")
    @RequestLog(value = "修改个人邮箱")
    public R<Long> updateEmail(@Validated @RequestBody ProfileEmailDto dto) {
        return R.success(ssoUserService.updateEmail(dto));
    }

    /**
     * 修改个人密码
     *
     * @param dto 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/updatePassword")
    @Operation(summary = "修改个人密码", description = "修改个人密码")
    @RequestLog(value = "修改个人密码")
    public R<Long> updatePassword(@Validated @RequestBody ProfilePasswordDto dto) {
        return R.success(ssoUserService.updatePassword(dto));
    }

    @Operation(summary = "发送短信验证码", description = "修改个人手机号并校验旧手机号，发送短信验证码")
    @Parameters({
            @Parameter(name = "phone", description = "手机号", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
            @Parameter(name = "oldPhone", description = "原手机号", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @GetMapping(value = "/sendPhoneCode")
    public R<String> sendPhoneCode(@RequestParam(value = "phone") String phone, @RequestParam(value = "oldPhone") String oldPhone) {
        return R.success(ssoUserService.sendPhoneCode(oldPhone, phone));
    }

    @Parameters({
            @Parameter(name = "email", description = "邮箱", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
            @Parameter(name = "oldEmail", description = "原邮箱", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @Operation(summary = "发送邮箱验证码", description = "修改个人邮箱并校验旧邮箱，发送邮箱验证码")
    @GetMapping(value = "/sendEmailCode")
    public R<String> sendEmailCode(@RequestParam(value = "email") String email, @RequestParam(value = "oldEmail") String oldEmail) {
        return R.success(ssoUserService.sendEmailCode(oldEmail, email));
    }
}
