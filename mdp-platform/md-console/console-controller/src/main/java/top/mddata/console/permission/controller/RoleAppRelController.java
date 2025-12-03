package top.mddata.console.permission.controller;

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
import top.mddata.console.permission.dto.RoleAppRelDto;
import top.mddata.console.permission.entity.RoleAppRel;
import top.mddata.console.permission.service.RoleAppRelService;

/**
 * 角色应用关联 控制层。
 *
 * @author henhen6
 * @since 2025-12-03 14:54:25
 */
@RestController
@Validated
@Tag(name = "角色应用关联")
@RequestMapping("/permission/roleAppRel")
@RequiredArgsConstructor
public class RoleAppRelController extends SuperController<RoleAppRelService, RoleAppRel> {

    /**
     * 添加角色应用关联。
     *
     * @param dto 角色应用关联
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存角色应用关联")
    @RequestLog(value = "新增", request = false)
    public R<Boolean> save(@Validated @RequestBody RoleAppRelDto dto) {
        return R.success(superService.saveByDto(dto));
    }

    /**
     * 根据主键删除角色应用关联。
     *
     * @param dto 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "删除角色应用关联")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@Validated @RequestBody RoleAppRelDto dto) {
        return R.success(superService.delete(dto));
    }

}
