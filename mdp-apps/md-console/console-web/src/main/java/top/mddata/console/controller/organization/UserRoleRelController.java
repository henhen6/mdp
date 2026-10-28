package top.mddata.console.controller.organization;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.console.dto.organization.UserRoleRelDto;
import top.mddata.console.dto.organization.UserRoleSaveDto;
import top.mddata.console.service.organization.UserRoleRelService;
import top.mddata.console.vo.organization.AssignableRoleVo;

import java.util.List;

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
    @RequestLog(value = "新增", logType = RequestLog.LogType.ADD, request = false)
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
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@Validated @RequestBody UserRoleRelDto dto) {
        return R.success(superService.delete(dto));
    }

    /**
     * 查询用户已拥有的角色ID（可分配范围内，分配角色弹窗回显）。
     *
     * @param userId 用户id
     * @return 角色ID集合
     */
    @GetMapping("/findRoleByUserId")
    @Operation(summary = "查询用户角色", description = "查询用户已拥有的角色ID（可分配范围内）")
    @RequestLog(value = "'查询用户角色:' + #userId", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<Long>> findRoleByUserId(@RequestParam Long userId) {
        return R.success(superService.findRoleIdByUserId(userId));
    }

    /**
     * 查询当前操作者可分配的角色列表（分配角色弹窗数据源）。
     *
     * @return 可分配角色与操作者身份标识
     */
    @GetMapping("/listAssignableRoles")
    @Operation(summary = "查询可分配角色", description = "按当前操作者身份返回可分配角色（运营人员含所有性质的管理员与普通角色）")
    @RequestLog(value = "查询可分配角色", logType = RequestLog.LogType.QUERY, response = false)
    public R<AssignableRoleVo> listAssignableRoles() {
        return R.success(superService.listAssignableRoles());
    }

    /**
     * 全量覆盖保存用户角色（用户侧分配角色）。
     *
     * @param dto 用户-角色关系
     * @return {@code true} 保存成功
     */
    @PostMapping("/saveUserRole")
    @Operation(summary = "分配角色", description = "全量覆盖保存用户角色")
    @RequestLog(value = "'分配角色:' + #dto.userId", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Boolean> saveUserRole(@Validated @RequestBody UserRoleSaveDto dto) {
        return R.success(superService.saveUserRole(dto));
    }

}
