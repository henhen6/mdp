package top.mddata.open.service.notify.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.open.entity.admin.EventPush;
import top.mddata.open.entity.admin.EventPushLog;
import top.mddata.open.mapper.admin.EventPushLogMapper;
import top.mddata.open.mapper.admin.EventPushMapper;
import top.mddata.open.service.notify.pipeline.NotifyResult;
import top.mddata.open.service.notify.pipeline.NotifySender;
import top.mddata.open.service.notify.pipeline.NotifyTaskExecutor;
import top.mddata.open.service.notify.pipeline.RetryPolicy;

/**
 * 事件推送执行器：只负责 mdo_event_push / mdo_event_push_log 两张表的钩子实现。
 */
@Slf4j
@Component
public class EventPushTaskExecutor extends NotifyTaskExecutor<EventPushTaskView> {

    private final EventPushMapper eventPushMapper;
    private final EventPushLogMapper eventPushLogMapper;

    public EventPushTaskExecutor(NotifySender notifySender, RetryPolicy retryPolicy,
                                 EventPushMapper eventPushMapper, EventPushLogMapper eventPushLogMapper) {
        super(notifySender, retryPolicy);
        this.eventPushMapper = eventPushMapper;
        this.eventPushLogMapper = eventPushLogMapper;
    }

    @Override
    protected EventPushTaskView loadTask(Long taskId) {
        EventPush entity = eventPushMapper.selectOneById(taskId);
        return entity == null ? null : new EventPushTaskView(entity);
    }

    @Override
    protected void persist(EventPushTaskView task) {
        eventPushMapper.update(task.getEntity());
    }

    @Override
    protected void appendLog(EventPushTaskView task, NotifyResult result) {
        try {
            EventPushLog pushLog = new EventPushLog();
            pushLog.setEventPushId(task.getId());
            pushLog.setRequestData(result.getRequestBody());
            pushLog.setResponseData(result.getResponseBody());
            pushLog.setRequestTime(result.getRequestTime());
            pushLog.setResponseTime(result.getResponseTime());
            pushLog.setErrorMsg(result.getErrorMsg());
            pushLog.setExecStatus(task.getExecStatus());
            eventPushLogMapper.insert(pushLog);
        } catch (Exception e) {
            log.error("[事件推送] 写推送日志失败, eventPushId={}", task.getId(), e);
        }
    }
}
