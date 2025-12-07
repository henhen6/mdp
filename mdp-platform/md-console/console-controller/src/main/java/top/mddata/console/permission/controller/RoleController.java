package top.mddata.console.permission.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
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
import top.mddata.console.permission.dto.RoleDto;
import top.mddata.console.permission.dto.RoleResourceRelDto;
import top.mddata.console.permission.entity.Role;
import top.mddata.console.permission.query.RoleQuery;
import top.mddata.console.permission.service.RoleResourceRelService;
import top.mddata.console.permission.service.RoleService;
import top.mddata.console.permission.vo.RoleVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static top.mddata.common.constant.SwaggerConstants.DATA_TYPE_LONG;
import static top.mddata.common.constant.SwaggerConstants.DATA_TYPE_STRING;

/**
 * 角色 控制层。
 *
 * @author henhen6
 * @since 2025-12-01 00:12:36
 */
@RestController
@Validated
@Tag(name = "角色")
@RequestMapping("/permission/role")
@RequiredArgsConstructor
public class RoleController extends SuperController<RoleService, Role> {
    private final RoleResourceRelService roleResourceRelService;
    private final EchoService echoService;

    /**
     * 添加角色。
     *
     * @param dto 角色
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存角色")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody RoleDto dto) {
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
    @RequestLog("'删除:' + #ids")
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
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody RoleDto dto) {
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
    @RequestLog("'单体查询:' + #id")
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
    @RequestLog(value = "批量查询", response = false)
    public R<List<RoleVo>> list(@RequestBody @Validated RoleQuery params) {
        Role entity = BeanUtil.toBean(params, Role.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        wrapper.eq(Role::getTemplateRole, false).eq(Role::getRoleCategory, RoleCategoryEnum.NORMAL_ROLE.getCode());
        List<RoleVo> listVo = superService.listAs(wrapper, RoleVo.class);
        echoService.action(listVo);
        return R.success(listVo);
    }


    @Parameters({
            @Parameter(name = "id", description = "ID", schema = @Schema(type = DATA_TYPE_LONG), in = ParameterIn.QUERY),
            @Parameter(name = "code", description = "编码", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @Operation(summary = "检测编码是否存在", description = "检测编码是否存在")
    @GetMapping("/checkCode")
    @RequestLog(value = "检测编码是否存在")
    public R<Boolean> checkCode(@RequestParam String code, @RequestParam(required = false) Long id) {
        return R.success(superService.checkCode(code, id));
    }

    /**
     * 查询角色拥有的资源集合
     *
     * @param roleId 角色id
     * @return 新增结果
     */
    @Operation(summary = "查询角色拥有的资源集合")
    @GetMapping("/findResourceIdByRoleId")
    @RequestLog("查询角色拥有的资源集合")
    public R<Map<Long, Collection<Long>>> findResourceIdByRoleId(@RequestParam Long roleId) {
        return R.success(roleResourceRelService.findResourceIdByRoleId(roleId));
    }

    /**
     * 新增角色资源。
     *
     * @param dto 角色
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/saveRoleResource")
    @Operation(summary = "新增角色资源", description = "新增角色资源")
    @RequestLog(value = "新增角色资源", request = false)
    public R<Boolean> saveRoleResource(@Validated @RequestBody RoleResourceRelDto dto) {
        return R.success(roleResourceRelService.saveRoleResource(dto));
    }
}
