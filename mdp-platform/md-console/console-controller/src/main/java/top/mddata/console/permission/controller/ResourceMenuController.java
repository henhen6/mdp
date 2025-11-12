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
import top.mddata.console.permission.dto.ResourceMenuDto;
import top.mddata.console.permission.entity.ResourceMenu;
import top.mddata.console.permission.query.ResourceMenuQuery;
import top.mddata.console.permission.service.ResourceMenuService;
import top.mddata.console.permission.vo.ResourceMenuVo;

import java.util.List;

/**
 * 菜单 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@RestController
@Validated
@Tag(name = "菜单")
@RequestMapping("/permission/resourceMenu")
@RequiredArgsConstructor
public class ResourceMenuController extends SuperController<ResourceMenuService, ResourceMenu> {
    private final ResourceMenuService resourceMenuService;

    /**
     * 添加菜单。
     *
     * @param dto 菜单
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存菜单")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody ResourceMenuDto dto) {
        return R.success(resourceMenuService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除菜单。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除菜单")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(resourceMenuService.removeByIds(ids));
    }

    /**
     * 根据主键更新菜单。
     *
     * @param dto 菜单
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新菜单")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody ResourceMenuDto dto) {
        return R.success(resourceMenuService.updateDtoById(dto).getId());
    }

    /**
     * 根据菜单主键获取详细信息。
     *
     * @param id 菜单主键
     * @return 菜单详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取菜单")
    @RequestLog("'单体查询:' + #id")
    public R<ResourceMenuVo> get(@RequestParam Long id) {
        ResourceMenu entity = resourceMenuService.getById(id);
        return R.success(BeanUtil.toBean(entity, ResourceMenuVo.class));
    }

    /**
     * 分页查询菜单。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询菜单")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<ResourceMenuVo>> page(@RequestBody @Validated PageParams<ResourceMenuQuery> params) {
        Page<ResourceMenuVo> page = Page.of(params.getCurrent(), params.getSize());
        ResourceMenu entity = BeanUtil.toBean(params.getModel(), ResourceMenu.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        resourceMenuService.pageAs(page, wrapper, ResourceMenuVo.class);
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
    public R<List<ResourceMenuVo>> list(@RequestBody @Validated ResourceMenuQuery params) {
        ResourceMenu entity = BeanUtil.toBean(params, ResourceMenu.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<ResourceMenuVo> listVo = resourceMenuService.listAs(wrapper, ResourceMenuVo.class);
        return R.success(listVo);
    }
}
