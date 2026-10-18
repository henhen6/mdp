package top.mddata.console.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.common.entity.User;
import top.mddata.console.service.organization.UserService;

/**
 * 内部-用户入口：仅供平台内部服务（workbench 等）经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-用户")
@RequestMapping("/inner/organization/user")
@RequiredArgsConstructor
public class InnerUserController {

    private final UserService userService;

    /**
     * 根据邮箱注册账号
     * @param ssoUser 用户
     */
    @Operation(hidden = true)
    @PostMapping("/registerByEmail")
    public R<Boolean> registerByEmail(@Validated @RequestBody User ssoUser) {
        return R.success(userService.registerByEmail(ssoUser));
    }

    /**
     * 根据手机注册账号
     * @param ssoUser 用户
     */
    @Operation(hidden = true)
    @PostMapping("/registerByPhone")
    public R<Boolean> registerByPhone(@Validated @RequestBody User ssoUser) {
        return R.success(userService.registerByPhone(ssoUser));
    }

    /**
     * 注册账号
     * @param defUser 用户信息
     */
    @Operation(hidden = true)
    @PostMapping("/registerByUsername")
    public R<Boolean> registerByUsername(@Validated @RequestBody User defUser) {
        return R.success(userService.registerByUsername(defUser));
    }
}
