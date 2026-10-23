package top.mddata.gateway.sop.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.mddata.open.entity.admin.ApiCallLog;
import top.mddata.open.mapper.admin.ApiCallLogMapper;

/**
 * 调用日志事件监听
 * @author henhen
 * @since 2026/1/12 23:29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallLogListener {
    private final ApiCallLogMapper apiCallLogMapper;

    /**
     * 异步落库：日志写失败只记 error，绝不影响业务响应（旧版同步 insert，DB 异常会冲掉响应）。
     */
    @Async("apiCallLogExecutor")
    @EventListener({CallLogEvent.class})
    public void saveApiCallLog(CallLogEvent event) {
        try {
            ApiCallLog source = (ApiCallLog) event.getSource();
            apiCallLogMapper.insert(source);
        } catch (Exception e) {
            log.error("保存接口调用日志失败", e);
        }
    }
}
