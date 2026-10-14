package top.mddata.worker.processors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import top.mddata.open.service.notify.callback.NotifyInfoService;

import java.time.LocalDateTime;

/**
 * 接口回调重试扫描：替代原 DubboConfiguration 中的 @Scheduled（该任务曾被 dubbo.enabled 开关错误控制）。
 * PowerJob 单机任务模式，多实例部署天然只有一个节点执行，无需自实现分布式锁。
 * 建议 cron：0 0/10 * * * ?（沿用原 10 分钟周期）
 */
@Component
@RequiredArgsConstructor
public class NotifyInfoRetryProcessor implements BasicProcessor {

    /** 单次扫描批量上限，防止一次加载全量失败记录 */
    private static final int BATCH_LIMIT = 500;

    private final NotifyInfoService notifyInfoService;

    @Override
    public ProcessResult process(TaskContext context) {
        notifyInfoService.retry(LocalDateTime.now(), BATCH_LIMIT);
        return new ProcessResult(true, "接口回调重试扫描完成");
    }
}
