package top.mddata.open.service.notify.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.open.entity.admin.NotifyInfo;
import top.mddata.open.entity.admin.NotifyInfoLog;
import top.mddata.open.mapper.admin.NotifyInfoLogMapper;
import top.mddata.open.mapper.admin.NotifyInfoMapper;
import top.mddata.open.service.notify.pipeline.NotifyResult;
import top.mddata.open.service.notify.pipeline.NotifySender;
import top.mddata.open.service.notify.pipeline.NotifyTaskExecutor;
import top.mddata.open.service.notify.pipeline.RetryPolicy;

/**
 * 接口回调执行器：只负责 mdo_notify_info / mdo_notify_info_log 两张表的钩子实现。
 */
@Slf4j
@Component
public class NotifyInfoTaskExecutor extends NotifyTaskExecutor<NotifyInfoTaskView> {

    private final NotifyInfoMapper notifyInfoMapper;
    private final NotifyInfoLogMapper notifyInfoLogMapper;

    public NotifyInfoTaskExecutor(NotifySender notifySender, RetryPolicy retryPolicy,
                                  NotifyInfoMapper notifyInfoMapper, NotifyInfoLogMapper notifyInfoLogMapper) {
        super(notifySender, retryPolicy);
        this.notifyInfoMapper = notifyInfoMapper;
        this.notifyInfoLogMapper = notifyInfoLogMapper;
    }

    @Override
    protected NotifyInfoTaskView loadTask(Long taskId) {
        NotifyInfo entity = notifyInfoMapper.selectOneById(taskId);
        return entity == null ? null : new NotifyInfoTaskView(entity);
    }

    @Override
    protected void persist(NotifyInfoTaskView task) {
        notifyInfoMapper.update(task.getEntity());
    }

    @Override
    protected void appendLog(NotifyInfoTaskView task, NotifyResult result) {
        try {
            NotifyInfoLog notifyLog = new NotifyInfoLog();
            notifyLog.setNotifyInfoId(task.getId());
            notifyLog.setRequestData(result.getRequestBody());
            notifyLog.setResponseData(result.getResponseBody());
            notifyLog.setRequestTime(result.getRequestTime());
            notifyLog.setResponseTime(result.getResponseTime());
            notifyLog.setErrorMsg(result.getErrorMsg());
            notifyLog.setExecStatus(task.getExecStatus());
            notifyInfoLogMapper.insert(notifyLog);
        } catch (Exception e) {
            // 日志写失败不冲掉已完成的投递与状态更新
            log.error("[回调] 写回调日志失败, notifyId={}", task.getId(), e);
        }
    }
}
