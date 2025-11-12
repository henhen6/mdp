package top.mddata.console.permission.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
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
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.console.permission.dto.RoleResourceRelDto;
import top.mddata.console.permission.entity.RoleResourceRel;
import top.mddata.console.permission.query.RoleResourceRelQuery;
import top.mddata.console.permission.service.RoleResourceRelService;
import top.mddata.console.permission.vo.RoleResourceRelVo;

import java.util.List;

/**
 * 角色资源关联 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:29
 */
@RestController
@Validated
@Tag(name = "角色资源关联")
@RequestMapping("/permission/roleResourceRel")
@RequiredArgsConstructor
public class RoleResourceRelController extends SuperController<RoleResourceRelService, RoleResourceRel> {
    private final RoleResourceRelService roleResourceRelService;

    /**
     * 添加角色资源关联。
     *
     * @param dto 角色资源关联
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存角色资源关联")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody RoleResourceRelDto dto) {
        return R.success(roleResourceRelService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除角色资源关联。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除角色资源关联")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(roleResourceRelService.removeByIds(ids));
    }

    /**
     * 根据主键更新角色资源关联。
     *
     * @param dto 角色资源关联
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新角色资源关联")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody RoleResourceRelDto dto) {
        return R.success(roleResourceRelService.updateDtoById(dto).getId());
    }

    /**
     * 根据角色资源关联主键获取详细信息。
     *
     * @param id 角色资源关联主键
     * @return 角色资源关联详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取角色资源关联")
    @RequestLog("'单体查询:' + #id")
    public R<RoleResourceRelVo> get(@RequestParam Long id) {
        RoleResourceRel entity = roleResourceRelService.getById(id);
        return R.success(BeanUtil.toBean(entity, RoleResourceRelVo.class));
    }

    /**
     * 分页查询角色资源关联。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询角色资源关联")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<RoleResourceRelVo>> page(@RequestBody @Validated PageParams<RoleResourceRelQuery> params) {
        Page<RoleResourceRelVo> page = Page.of(params.getCurrent(), params.getSize());
        RoleResourceRel entity = BeanUtil.toBean(params.getModel(), RoleResourceRel.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        roleResourceRelService.pageAs(page, wrapper, RoleResourceRelVo.class);
        return R.success(page);
    }

    /**
     * 批量查询
     * @param params 查询参数
     * @return 集合
     */
    @PostMapping("/list")
    @Operation(summary = "批量查询", description = "批量查询")
    @RequestLog(value = "批量查询", response = false)
    public R<List<RoleResourceRelVo>> list(@RequestBody @Validated RoleResourceRelQuery params) {
        RoleResourceRel entity = BeanUtil.toBean(params, RoleResourceRel.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<RoleResourceRelVo> listVo = roleResourceRelService.listAs(wrapper, RoleResourceRelVo.class);
        return R.success(listVo);
    }
}
