package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.workbench.dto.ProfileUserDto;
import top.mddata.workbench.service.SsoUserService;

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
}
