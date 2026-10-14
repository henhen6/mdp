package top.mddata.open.service.notify.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 推送任务调度入口：把「任务ID + 执行器」投递到统一线程池。
 * 拒绝策略为 AbortPolicy：线程池满时不阻塞调用线程（修旧版 CallerRuns 导致调度线程被 HTTP 阻塞的问题），
 * 任务保持原状态（WAIT/FAIL），由 PowerJob 重试扫描兜底，不丢任务。
 */
@Slf4j
@Component
public class NotifyTaskDispatcher {

    private final ThreadPoolExecutor threadPool;

    public NotifyTaskDispatcher(@Qualifier("notifyTaskThreadPool") ThreadPoolExecutor threadPool) {
        this.threadPool = threadPool;
    }

    public void submit(Long taskId, NotifyTaskExecutor<? extends NotifyTaskView> executor) {
        try {
            threadPool.execute(() -> executor.execute(taskId));
        } catch (RejectedExecutionException e) {
            log.warn("[推送] 线程池已满，任务留待重试扫描兜底, taskId={}", taskId);
        }
    }

    public void submitAll(Collection<Long> taskIds, NotifyTaskExecutor<? extends NotifyTaskView> executor) {
        taskIds.forEach(taskId -> submit(taskId, executor));
    }
}
