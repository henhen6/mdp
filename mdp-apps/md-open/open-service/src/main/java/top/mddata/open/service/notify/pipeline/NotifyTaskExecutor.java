package top.mddata.open.service.notify.pipeline;

import lombok.extern.slf4j.Slf4j;
import top.mddata.open.enumeration.admin.ExecStatusEnum;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 投递生命周期模板：取任务 → 幂等检查 → 投递 → 状态流转 → 持久化 → 写日志。
 * 子类只需实现三个钩子（各自操作自己的任务表/日志表），管道逻辑全类唯一一份。
 *
 * @param <T> 任务视图类型（mdo_notify_info / mdo_event_push 各自的适配器）
 */
@Slf4j
public abstract class NotifyTaskExecutor<T extends NotifyTaskView> {

    /** 终态：重复提交（手动重推连点、重试扫描与即时投递并发）不产生重复投递 */
    private static final Set<String> TERMINAL_STATUS = Set.of(
            ExecStatusEnum.SUCCESS.getCode(), ExecStatusEnum.RETRY_OVER.getCode(), ExecStatusEnum.END.getCode());

    private final NotifySender notifySender;
    private final RetryPolicy retryPolicy;

    protected NotifyTaskExecutor(NotifySender notifySender, RetryPolicy retryPolicy) {
        this.notifySender = notifySender;
        this.retryPolicy = retryPolicy;
    }

    public final void execute(Long taskId) {
        try {
            T task = loadTask(taskId);
            if (task == null) {
                log.warn("[推送] 任务不存在，跳过, taskId={}", taskId);
                return;
            }
            if (TERMINAL_STATUS.contains(task.getExecStatus())) {
                log.info("[推送] 任务已是终态，跳过, taskId={}, status={}", taskId, task.getExecStatus());
                return;
            }
            task.markDelivering(LocalDateTime.now());
            NotifyResult result = notifySender.send(task);
            if (result.isSuccess()) {
                task.markStatus(ExecStatusEnum.SUCCESS.getCode(), null);
            } else {
                LocalDateTime nextTime = retryPolicy.nextRequestTime(task.getRequestCnt(), LocalDateTime.now());
                task.markStatus(nextTime == null ? ExecStatusEnum.RETRY_OVER.getCode()
                        : ExecStatusEnum.FAIL.getCode(), nextTime);
            }
            persist(task);
            appendLog(task, result);
        } catch (Exception e) {
            // 兜底：线程池任务异常不外抛，任务保持原状态等下轮重试扫描
            log.error("[推送] 投递生命周期异常, taskId={}", taskId, e);
        }
    }

    protected abstract T loadTask(Long taskId);

    protected abstract void persist(T task);

    protected abstract void appendLog(T task, NotifyResult result);
}
