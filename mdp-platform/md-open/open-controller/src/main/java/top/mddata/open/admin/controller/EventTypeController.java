package top.mddata.open.admin.controller;

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
import top.mddata.open.admin.dto.EventTypeDto;
import top.mddata.open.admin.entity.EventType;
import top.mddata.open.admin.query.EventTypeQuery;
import top.mddata.open.admin.service.EventTypeService;
import top.mddata.open.admin.vo.EventTypeVo;

import java.util.List;

/**
 * 事件类型 控制层。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@RestController
@Validated
@Tag(name = "事件类型")
@RequestMapping("/admin/eventType")
@RequiredArgsConstructor
public class EventTypeController extends SuperController<EventTypeService, EventType> {
    /**
     * 添加事件类型。
     *
     * @param dto 事件类型
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存事件类型")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody EventTypeDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除事件类型。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除事件类型")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新事件类型。
     *
     * @param dto 事件类型
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新事件类型")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody EventTypeDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 检测编码是否存在。
     *
     * @param code 事件编码
     * @param id 主键id
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/check")
    @Operation(summary = "检测编码是否存在", description = "检测编码是否存在")
    @RequestLog(value = "检测编码是否存在", request = false)
    public R<Boolean> check(@RequestParam String code, @RequestParam(required = false) Long id) {
        return R.success(superService.check(code, id));
    }

    /**
     * 根据事件类型主键获取详细信息。
     *
     * @param id 事件类型主键
     * @return 事件类型详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取事件类型")
    @RequestLog("'单体查询:' + #id")
    public R<EventTypeVo> get(@RequestParam Long id) {
        EventType entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, EventTypeVo.class));
    }

    /**
     * 分页查询事件类型。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询事件类型")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<EventTypeVo>> page(@RequestBody @Validated PageParams<EventTypeQuery> params) {
        Page<EventTypeVo> page = Page.of(params.getCurrent(), params.getSize());
        EventType entity = BeanUtil.toBean(params.getModel(), EventType.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, EventTypeVo.class);
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
    public R<List<EventTypeVo>> list(@RequestBody @Validated EventTypeQuery params) {
        EventType entity = BeanUtil.toBean(params, EventType.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<EventTypeVo> listVo = superService.listAs(wrapper, EventTypeVo.class);
        return R.success(listVo);
    }
}
