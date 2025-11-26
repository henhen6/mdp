package top.mddata.open.admin.controller;

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
import top.mddata.open.admin.dto.GroupScopeRelDto;
import top.mddata.open.admin.entity.GroupScopeRel;
import top.mddata.open.admin.service.GroupScopeRelService;

/**
 * 分组拥有的oauth2权限 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@RestController
@Validated
@Tag(name = "分组拥有的oauth2权限")
@RequestMapping("/admin/groupScopeRel")
@RequiredArgsConstructor
public class GroupScopeRelController extends SuperController<GroupScopeRelService, GroupScopeRel> {
    /**
     * 添加分组拥有的oauth2权限。
     *
     * @param dto 分组拥有的oauth2权限
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存分组拥有的oauth2权限")
    @RequestLog(value = "新增", request = false)
    public R<Boolean> save(@Validated @RequestBody GroupScopeRelDto dto) {
        superService.saveDto(dto);
        return R.success(true);
    }

    /**
     * 根据主键删除分组拥有的oauth2权限。
     *
     * @param dto 待删除信息
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除分组拥有的oauth2权限")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@Validated @RequestBody GroupScopeRelDto dto) {
        return R.success(superService.delete(dto.getGroupId(), dto.getScopeIdList()));
    }

}
