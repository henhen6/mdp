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
import top.mddata.console.permission.dto.ResourceFieldDto;
import top.mddata.console.permission.entity.ResourceField;
import top.mddata.console.permission.query.ResourceFieldQuery;
import top.mddata.console.permission.service.ResourceFieldService;
import top.mddata.console.permission.vo.ResourceFieldVo;

import java.util.List;

/**
 * 字段权限 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 19:49:35
 */
@RestController
@Validated
@Tag(name = "字段权限")
@RequestMapping("/permission/resourceField")
@RequiredArgsConstructor
public class ResourceFieldController extends SuperController<ResourceFieldService, ResourceField> {
    /**
     * 添加字段权限。
     *
     * @param dto 字段权限
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存字段权限")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody ResourceFieldDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除字段权限。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除字段权限")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新字段权限。
     *
     * @param dto 字段权限
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新字段权限")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody ResourceFieldDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据字段权限主键获取详细信息。
     *
     * @param id 字段权限主键
     * @return 字段权限详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取字段权限")
    @RequestLog("'单体查询:' + #id")
    public R<ResourceFieldVo> get(@RequestParam Long id) {
        ResourceField entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, ResourceFieldVo.class));
    }

    /**
     * 分页查询字段权限。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询字段权限")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<ResourceFieldVo>> page(@RequestBody @Validated PageParams<ResourceFieldQuery> params) {
        Page<ResourceFieldVo> page = Page.of(params.getCurrent(), params.getSize());
        ResourceField entity = BeanUtil.toBean(params.getModel(), ResourceField.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, ResourceFieldVo.class);
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
    public R<List<ResourceFieldVo>> list(@RequestBody @Validated ResourceFieldQuery params) {
        ResourceField entity = BeanUtil.toBean(params, ResourceField.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<ResourceFieldVo> listVo = superService.listAs(wrapper, ResourceFieldVo.class);
        return R.success(listVo);
    }
}
