package top.mddata.open.controller.admin;

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
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.open.dto.admin.EventTriggerDto;
import top.mddata.open.entity.admin.EventTrigger;
import top.mddata.open.query.admin.EventTriggerQuery;
import top.mddata.open.service.admin.EventTriggerService;
import top.mddata.open.vo.admin.EventTriggerVo;

import java.util.List;

/**
 * 事件触发 控制层。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@RestController
@Validated
@Tag(name = "事件触发")
@RequestMapping("/admin/eventTrigger")
@RequiredArgsConstructor
public class EventTriggerController extends SuperController<EventTriggerService, EventTrigger> {

    /**
     * 根据主键删除事件触发。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除事件触发")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据事件触发主键获取详细信息。
     *
     * @param id 事件触发主键
     * @return 事件触发详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取事件触发")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<EventTriggerVo> get(@RequestParam Long id) {
        EventTrigger entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, EventTriggerVo.class));
    }

    /**
     * 分页查询事件触发。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询事件触发")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<EventTriggerVo>> page(@RequestBody @Validated PageParams<EventTriggerQuery> params) {
        Page<EventTriggerVo> page = Page.of(params.getCurrent(), params.getSize());
        EventTrigger entity = BeanUtil.toBean(params.getModel(), EventTrigger.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, EventTriggerVo.class);
        return R.success(page);
    }

    /**
     * 批量查询
     * @param params 查询参数
     * @return 集合
     */
    @PostMapping("/list")
    @Operation(summary = "批量查询", description = "批量查询")
    @RequestLog(value = "批量查询", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<EventTriggerVo>> list(@RequestBody @Validated EventTriggerQuery params) {
        EventTrigger entity = BeanUtil.toBean(params, EventTrigger.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<EventTriggerVo> listVo = superService.listAs(wrapper, EventTriggerVo.class);
        return R.success(listVo);
    }

    /**
     * 保存事件触发
     *
     * @param save 事件触发参数
     * @return 事件触发
     */
    @PostMapping("/save")
    @Operation(hidden = true)
    @RequestLog(value = "保存事件触发", logType = RequestLog.LogType.ADD)
    public R<EventTrigger> save(@RequestBody EventTriggerDto save) {
        return R.success(superService.save(save));
    }

}
