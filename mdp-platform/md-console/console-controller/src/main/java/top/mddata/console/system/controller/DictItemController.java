package top.mddata.console.system.controller;

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
import top.mddata.console.system.dto.DictItemDto;
import top.mddata.console.system.entity.DictItem;
import top.mddata.console.system.query.DictItemQuery;
import top.mddata.console.system.service.DictItemService;
import top.mddata.console.system.vo.DictItemVo;

import java.util.List;

/**
 * 字典项 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@RestController
@Validated
@Tag(name = "字典项")
@RequestMapping("/system/dictItem")
@RequiredArgsConstructor
public class DictItemController extends SuperController<DictItemService, DictItem> {
    private final DictItemService dictItemService;

    /**
     * 添加字典项。
     *
     * @param dto 字典项
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存字典项")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody DictItemDto dto) {
        return R.success(dictItemService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除字典项。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除字典项")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(dictItemService.removeByIds(ids));
    }

    /**
     * 根据主键更新字典项。
     *
     * @param dto 字典项
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新字典项")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody DictItemDto dto) {
        return R.success(dictItemService.updateDtoById(dto).getId());
    }

    /**
     * 根据字典项主键获取详细信息。
     *
     * @param id 字典项主键
     * @return 字典项详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取字典项")
    @RequestLog("'单体查询:' + #id")
    public R<DictItemVo> get(@RequestParam Long id) {
        DictItem entity = dictItemService.getById(id);
        return R.success(BeanUtil.toBean(entity, DictItemVo.class));
    }

    /**
     * 分页查询字典项。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询字典项")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<DictItemVo>> page(@RequestBody @Validated PageParams<DictItemQuery> params) {
        Page<DictItemVo> page = Page.of(params.getCurrent(), params.getSize());
        DictItem entity = BeanUtil.toBean(params.getModel(), DictItem.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        dictItemService.pageAs(page, wrapper, DictItemVo.class);
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
    public R<List<DictItemVo>> list(@RequestBody @Validated DictItemQuery params) {
        DictItem entity = BeanUtil.toBean(params, DictItem.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<DictItemVo> listVo = dictItemService.listAs(wrapper, DictItemVo.class);
        return R.success(listVo);
    }
}
