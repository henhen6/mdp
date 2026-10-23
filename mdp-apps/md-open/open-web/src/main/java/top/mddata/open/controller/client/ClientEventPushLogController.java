package top.mddata.open.controller.client;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
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
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.open.controller.support.PageWrapperHelper;
import top.mddata.open.entity.admin.EventPush;
import top.mddata.open.entity.admin.EventPushLog;
import top.mddata.open.query.admin.EventPushLogQuery;
import top.mddata.open.service.notify.event.EventPushLogService;
import top.mddata.open.service.notify.event.EventPushService;
import top.mddata.open.vo.admin.EventPushLogVo;

import java.util.List;

/**
 * 我的事件推送日志（开发者自助查询，只读）。
 *
 * @author henhen6
 */
@RestController
@Validated
@Tag(name = "我的事件推送日志")
@RequestMapping("/client/eventPushLog")
@RequiredArgsConstructor
public class ClientEventPushLogController {

    private final EventPushLogService eventPushLogService;
    private final EventPushService eventPushService;
    private final ClientAppHelper clientAppHelper;

    /**
     * 分页查询当前开发者的事件推送日志。传了 eventPushId 时先校验推送任务归属；
     * 未传时兜底限制在本人应用的推送日志内。
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询当前开发者的事件推送日志")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<EventPushLogVo>> page(@RequestBody @Validated PageParams<EventPushLogQuery> params) {
        Page<EventPushLogVo> page = Page.of(params.getCurrent(), params.getSize());
        QueryWrapper wrapper = PageWrapperHelper.build(params, EventPushLog.class);
        Long eventPushId = params.getModel() == null ? null : params.getModel().getEventPushId();
        if (eventPushId != null) {
            // 越权直接失败，不做静默降级
            EventPush eventPush = eventPushService.getById(eventPushId);
            ArgumentAssert.notNull(eventPush, "推送任务不存在");
            clientAppHelper.checkOwner(eventPush.getAppId());
        } else {
            List<Long> appIds = clientAppHelper.myAppIds();
            if (appIds.isEmpty()) {
                return R.success(page);
            }
            wrapper.and(EventPushLog::getEventPushId).in(QueryWrapper.create()
                    .select(EventPush::getId)
                    .from(EventPush.class)
                    .where(EventPush::getAppId).in(appIds));
        }
        eventPushLogService.pageAs(page, wrapper, EventPushLogVo.class);
        return R.success(page);
    }
}
