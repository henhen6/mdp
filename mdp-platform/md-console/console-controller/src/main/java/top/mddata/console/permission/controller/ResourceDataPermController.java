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
import top.mddata.console.permission.dto.ResourceDataPermDto;
import top.mddata.console.permission.entity.ResourceDataPerm;
import top.mddata.console.permission.query.ResourceDataPermQuery;
import top.mddata.console.permission.service.ResourceDataPermService;
import top.mddata.console.permission.vo.ResourceDataPermVo;

import java.util.List;

/**
 * 数据权限 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@RestController
@Validated
@Tag(name = "数据权限")
@RequestMapping("/permission/resourceDataPerm")
@RequiredArgsConstructor
public class ResourceDataPermController extends SuperController<ResourceDataPermService, ResourceDataPerm> {
    private final ResourceDataPermService resourceDataPermService;

    /**
     * 添加数据权限。
     *
     * @param dto 数据权限
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存数据权限")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody ResourceDataPermDto dto) {
        return R.success(resourceDataPermService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除数据权限。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除数据权限")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(resourceDataPermService.removeByIds(ids));
    }

    /**
     * 根据主键更新数据权限。
     *
     * @param dto 数据权限
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新数据权限")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody ResourceDataPermDto dto) {
        return R.success(resourceDataPermService.updateDtoById(dto).getId());
    }

    /**
     * 根据数据权限主键获取详细信息。
     *
     * @param id 数据权限主键
     * @return 数据权限详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取数据权限")
    @RequestLog("'单体查询:' + #id")
    public R<ResourceDataPermVo> get(@RequestParam Long id) {
        ResourceDataPerm entity = resourceDataPermService.getById(id);
        return R.success(BeanUtil.toBean(entity, ResourceDataPermVo.class));
    }

    /**
     * 分页查询数据权限。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询数据权限")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<ResourceDataPermVo>> page(@RequestBody @Validated PageParams<ResourceDataPermQuery> params) {
        Page<ResourceDataPermVo> page = Page.of(params.getCurrent(), params.getSize());
        ResourceDataPerm entity = BeanUtil.toBean(params.getModel(), ResourceDataPerm.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        resourceDataPermService.pageAs(page, wrapper, ResourceDataPermVo.class);
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
    public R<List<ResourceDataPermVo>> list(@RequestBody @Validated ResourceDataPermQuery params) {
        ResourceDataPerm entity = BeanUtil.toBean(params, ResourceDataPerm.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<ResourceDataPermVo> listVo = resourceDataPermService.listAs(wrapper, ResourceDataPermVo.class);
        return R.success(listVo);
    }
}
