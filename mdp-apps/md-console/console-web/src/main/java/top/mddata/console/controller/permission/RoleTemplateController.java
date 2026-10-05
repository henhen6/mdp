package top.mddata.console.controller.permission;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
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
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.interfaces.echo.EchoService;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.dto.permission.RoleDataScopeRelDto;
import top.mddata.console.dto.permission.RoleTemploateDto;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.query.permission.RoleQuery;
import top.mddata.console.service.permission.RoleDataScopeRelService;
import top.mddata.console.service.permission.RoleFieldRelService;
import top.mddata.console.service.permission.RoleTemplateService;
import top.mddata.console.vo.permission.RoleFieldAuthVo;
import top.mddata.console.vo.permission.RoleVo;

import java.util.List;

/**
 * 角色模版 控制层。
 *
 * @author henhen6
 * @since 2025-12-01 00:12:36
 */
@RestController
@Validated
@Tag(name = "角色模版")
@RequestMapping("/permission/roleTemplate")
@RequiredArgsConstructor
public class RoleTemplateController extends SuperController<RoleTemplateService, Role> {
    private final RoleDataScopeRelService roleDataScopeRelService;
    private final RoleFieldRelService roleFieldRelService;
    private final EchoService echoService;

    /**
     * 添加角色。
     *
     * @param dto 角色
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存角色")
    @RequestLog(value = "新增", logType = RequestLog.LogType.ADD, request = false)
    public R<Long> save(@Validated @RequestBody RoleTemploateDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除角色。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除角色")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新角色。
     *
     * @param dto 角色
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新角色")
    @RequestLog(value = "修改", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody RoleTemploateDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据角色主键获取详细信息。
     *
     * @param id 角色主键
     * @return 角色详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取角色")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<RoleVo> get(@RequestParam Long id) {
        Role entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, RoleVo.class));
    }


    /**
     * 批量查询
     * @param params 查询参数
     * @return 集合
     */
    @PostMapping("/list")
    @Operation(summary = "批量查询", description = "批量查询")
    @RequestLog(value = "批量查询", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<RoleVo>> list(@RequestBody @Validated RoleQuery params) {
        Role entity = BeanUtil.toBean(params, Role.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        wrapper.eq(Role::getTemplateRole, true).in(Role::getRoleCategory, RoleCategoryEnum.ADMIN_ROLE.getCode(), RoleCategoryEnum.PERM_SET.getCode()).orderBy(Role::getCreatedAt, false);
        List<RoleVo> listVo = superService.listAs(wrapper, RoleVo.class);
        echoService.action(listVo);
        return R.success(listVo);
    }

    /**
     * 保存角色的数据权限授权。
     *
     * @param dto 授权项
     * @return {@code true} 成功
     */
    @PostMapping("/saveRoleDataScope")
    @Operation(summary = "保存角色数据权限", description = "全量覆盖保存角色的数据权限授权")
    @RequestLog(value = "保存角色数据权限", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Boolean> saveRoleDataScope(@Validated @RequestBody RoleDataScopeRelDto dto) {
        return R.success(roleDataScopeRelService.saveRoleDataScope(dto));
    }

    /**
     * 字段权限授权页面聚合数据（角色模板：应用全量菜单 + 全部启用字段）。
     *
     * @param roleId 角色模板ID
     * @return 按应用分组的菜单字段树及各应用已受限字段
     */
    @GetMapping("/fieldAuthTree")
    @Operation(summary = "字段权限授权聚合数据", description = "按应用分组返回菜单字段树及已受限字段（模板角色取全量资源）")
    @RequestLog(value = "查询字段权限授权聚合数据", logType = RequestLog.LogType.QUERY)
    public R<RoleFieldAuthVo> fieldAuthTree(@RequestParam Long roleId) {
        return R.success(roleFieldRelService.fieldAuthTree(roleId, true));
    }

}
