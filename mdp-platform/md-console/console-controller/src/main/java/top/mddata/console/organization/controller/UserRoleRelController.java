package top.mddata.console.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.console.organization.dto.UserRoleRelDto;
import top.mddata.console.organization.service.UserRoleRelService;

/**
 * 用户角色关联 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 20:03:50
 */
@RestController
@Validated
@Tag(name = "用户角色关联")
@RequestMapping("/organization/userRoleRel")
@RequiredArgsConstructor
public class UserRoleRelController extends SuperController<UserRoleRelService, UserRoleRel> {
    /**
     * 添加用户角色关联。
     *
     * @param dto 用户角色关联
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存用户角色关联")
    @RequestLog(value = "新增", request = false)
    public R<Boolean> save(@Validated @RequestBody UserRoleRelDto dto) {
        return R.success(superService.saveByDto(dto));
    }

    /**
     * 根据主键删除用户角色关联。
     *
     * @param dto 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除用户角色关联")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@Validated @RequestBody UserRoleRelDto dto) {
        return R.success(superService.delete(dto));
    }

}
