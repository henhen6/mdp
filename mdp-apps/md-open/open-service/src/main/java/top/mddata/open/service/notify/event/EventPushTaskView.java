package top.mddata.open.service.notify.event;

import com.alibaba.fastjson2.JSONObject;
import com.gitee.sop.support.aes.MdpBizMsgCrypt;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.mddata.open.entity.admin.EventPush;
import top.mddata.open.enumeration.admin.EventTypeEnum;
import top.mddata.open.service.notify.pipeline.NotifyTaskView;

import java.time.LocalDateTime;

/**
 * 事件推送任务适配器：与回调的差异在业务报文（EVENT_PUSH + eventCode + eventTriggerId，无 version）。
 */
@Getter
@RequiredArgsConstructor
public class EventPushTaskView implements NotifyTaskView {

    private final EventPush entity;

    @Override
    public Long getId() {
        return entity.getId();
    }

    @Override
    public String getNotifyUrl() {
        return entity.getNotifyUrl();
    }

    @Override
    public String getAppKey() {
        return entity.getAppKey();
    }

    @Override
    public Integer getNotifyEncryptionType() {
        return entity.getNotifyEncryptionType();
    }

    @Override
    public String getNotifyToken() {
        return entity.getNotifyToken();
    }

    @Override
    public String getNotifyEncodingAesKey() {
        return entity.getNotifyEncodingAesKey();
    }

    @Override
    public Integer getRequestCnt() {
        return entity.getRequestCnt();
    }

    @Override
    public String getExecStatus() {
        return entity.getExecStatus();
    }

    @Override
    public String buildPlaintext() {
        JSONObject bizData = new JSONObject();
        bizData.put("type", EventTypeEnum.EVENT_PUSH.name());
        bizData.put("method", entity.getEventCode());
        bizData.put("appKey", entity.getAppKey());
        bizData.put("timestamp", MdpBizMsgCrypt.generateTimestamp());
        bizData.put("eventTriggerId", entity.getEventTriggerId());
        bizData.put("bizContent", entity.getRequestData());
        return bizData.toJSONString();
    }

    @Override
    public void markDelivering(LocalDateTime now) {
        entity.setRequestCnt(entity.getRequestCnt() + 1);
        entity.setLastRequestTime(now);
    }

    @Override
    public void markStatus(String execStatus, LocalDateTime nextRequestTime) {
        entity.setExecStatus(execStatus);
        entity.setNextRequestTime(nextRequestTime);
    }
}
