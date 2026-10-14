package top.mddata.open.service.notify.pipeline;

import java.time.LocalDateTime;

/**
 * 推送任务视图：公共管道只面向该接口编程，不感知具体任务表（mdo_notify_info / mdo_event_push）。
 * 两种任务实体各写一个适配器实现本接口。
 */
public interface NotifyTaskView {

    Long getId();

    String getNotifyUrl();

    String getAppKey();

    Integer getNotifyEncryptionType();

    String getNotifyToken();

    String getNotifyEncodingAesKey();

    Integer getRequestCnt();

    String getExecStatus();

    /** 组装业务报文（明文 JSON），两种任务的字段差异在此封装 */
    String buildPlaintext();

    /** 推进一次投递计数：requestCnt + 1，lastRequestTime = now */
    void markDelivering(LocalDateTime now);

    /** 更新执行状态与下次重试时间 */
    void markStatus(String execStatus, LocalDateTime nextRequestTime);
}
