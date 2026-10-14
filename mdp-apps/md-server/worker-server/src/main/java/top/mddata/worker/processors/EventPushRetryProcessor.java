package top.mddata.worker.processors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import top.mddata.open.service.notify.event.EventPushService;

import java.time.LocalDateTime;

/**
 * 事件推送重试扫描。
 * 建议 cron：0 0/8 * * * ?（沿用原 8 分钟周期）
 */
@Component
@RequiredArgsConstructor
public class EventPushRetryProcessor implements BasicProcessor {

    /** 单次扫描批量上限，防止一次加载全量失败记录 */
    private static final int BATCH_LIMIT = 500;

    private final EventPushService eventPushService;

    @Override
    public ProcessResult process(TaskContext context) {
        eventPushService.retry(LocalDateTime.now(), BATCH_LIMIT);
        return new ProcessResult(true, "事件推送重试扫描完成");
    }
}
