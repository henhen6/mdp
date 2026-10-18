package top.mddata.open.controller.notify.event;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.open.controller.support.PageWrapperHelper;
import top.mddata.open.entity.admin.EventTrigger;
import top.mddata.open.query.admin.EventTriggerQuery;
import top.mddata.open.service.notify.event.EventTriggerService;
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
        superService.pageAs(page, PageWrapperHelper.build(params, EventTrigger.class), EventTriggerVo.class);
        return R.success(page);
    }

}
