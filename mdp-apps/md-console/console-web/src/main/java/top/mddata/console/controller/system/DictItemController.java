package top.mddata.console.controller.system;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.console.dto.system.DictItemDto;
import top.mddata.console.entity.system.Dict;
import top.mddata.console.entity.system.DictItem;
import top.mddata.console.query.system.DictItemQuery;
import top.mddata.console.service.system.DictItemService;
import top.mddata.console.service.system.DictService;
import top.mddata.console.vo.system.DictItemVo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字典项 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 20:06:39
 */
@RestController
@Validated
@Tag(name = "字典项")
@RequestMapping("/system/dictItem")
@RequiredArgsConstructor
public class DictItemController extends SuperController<DictItemService, DictItem> {
    private final DictService dictService;

    /**
     * 添加字典项。
     *
     * @param dto 字典项
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存字典项")
    @RequestLog(value = "新增", logType = RequestLog.LogType.ADD, request = false)
    public R<Long> save(@Validated @RequestBody DictItemDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除字典项。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除字典项")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新字典项。
     *
     * @param dto 字典项
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新字典项")
    @RequestLog(value = "修改", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody DictItemDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 分页查询字典项。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询字典项")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<DictItemVo>> page(@RequestBody @Validated PageParams<DictItemQuery> params) {
        Page<DictItemVo> page = Page.of(params.getCurrent(), params.getSize());
        DictItem entity = BeanUtil.toBean(params.getModel(), DictItem.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, DictItemVo.class);
        return R.success(page);
    }

    /**
     * 批量查询
     * @param query 查询参数
     * @return 集合
     */
    @PostMapping("/list")
    @Operation(summary = "批量查询", description = "批量查询")
    @RequestLog(value = "批量查询", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<DictItemVo>> list(@RequestBody DictItemQuery query) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (StrUtil.isNotEmpty(query.getDictUniqKey())) {
            Dict sysDict = dictService.getOne(QueryWrapper.create().eq(Dict::getUniqKey, query.getDictUniqKey()));
            if (sysDict != null) {
                queryWrapper.eq(DictItem::getDictId, sysDict.getId());
            }
        }
        queryWrapper.orderBy(DictItem::getWeight, true);
        List<DictItemVo> listVo = superService.listAs(queryWrapper, DictItemVo.class);
        return R.success(listVo);
    }

    /**
     * 查询字典项树结构
     */
    @Operation(summary = "查询字典项-树结构", description = "查询字典项-树结构")
    @PostMapping("/tree")
    @RequestLog(value = "查询字典项-树结构", logType = RequestLog.LogType.QUERY)
    public R<List<DictItemVo>> tree(@RequestBody @Validated DictItemQuery query) {
        List<DictItemVo> list = superService.listAs(new QueryWrapper().eq(DictItem::getDictId, query.getDictId()).orderBy(DictItem::getWeight, true), DictItemVo.class);
        List<DictItemVo> menuTreeList = MyTreeUtil.buildTreeEntity(list, DictItemVo::new);
        return R.success(menuTreeList);
    }

    @Operation(summary = "查询字典项", description = "根据字典编码查询字典项")
    @PostMapping("/findByIds")
    public Map<Serializable, Object> findDictByIds(@RequestParam("ids") Set<Serializable> ids) {
        return this.superService.findByIds(ids);
    }
}
