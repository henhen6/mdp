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
import top.mddata.common.dto.IdDto;
import top.mddata.open.controller.support.PageWrapperHelper;
import top.mddata.open.entity.admin.EventPush;
import top.mddata.open.query.admin.EventPushQuery;
import top.mddata.open.service.notify.event.EventPushService;
import top.mddata.open.vo.admin.EventPushVo;

import java.util.List;

/**
 * 事件推送任务 控制层。
 *
 * @author henhen6
 * @since 2026-01-12 21:28:36
 */
@RestController
@Validated
@Tag(name = "事件推送任务")
@RequestMapping("/admin/eventPush")
@RequiredArgsConstructor
public class EventPushController extends SuperController<EventPushService, EventPush> {

    /**
     * 重新推送
     *
     * @param param 表单数据
     * @return 返回影响行数
     */
    @PostMapping("/push")
    @Operation(summary = "重新推送", description = "重新推送")
    @RequestLog(value = "'重新推送:' + #param.id", logType = RequestLog.LogType.OTHER)
    public R<Long> push(@Validated @RequestBody IdDto param) {
        return R.success(superService.executeImmediately(param.getId()));
    }

    /**
     * 结束重试
     *
     * @param param 表单数据
     * @return 返回影响行数
     */
    @Operation(summary = "结束重试", description = "结束重试")
    @PostMapping("/end")
    @RequestLog(value = "'结束重试:' + #param.id", logType = RequestLog.LogType.OTHER)
    public R<Boolean> end(@Validated @RequestBody IdDto param) {
        return R.success(superService.end(param.getId()));
    }

    /**
     * 根据主键删除事件推送任务。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除事件推送任务")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }


    /**
     * 分页查询事件推送任务。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询事件推送任务")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<EventPushVo>> page(@RequestBody @Validated PageParams<EventPushQuery> params) {
        Page<EventPushVo> page = Page.of(params.getCurrent(), params.getSize());
        superService.pageAs(page, PageWrapperHelper.build(params, EventPush.class), EventPushVo.class);
        return R.success(page);
    }
}
