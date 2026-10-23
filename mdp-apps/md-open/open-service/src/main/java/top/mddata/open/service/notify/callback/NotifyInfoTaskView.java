package top.mddata.open.service.notify.callback;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gitee.sop.support.aes.MdpBizMsgCrypt;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.mddata.open.dto.admin.NotifyInfoDto;
import top.mddata.open.entity.admin.NotifyInfo;
import top.mddata.open.enumeration.admin.EventTypeEnum;
import top.mddata.open.service.notify.pipeline.NotifyTaskView;

import java.time.LocalDateTime;

/**
 * 接口回调任务适配器：把 NotifyInfo 实体包装成管道视图，业务报文差异在此封装。
 */
@Getter
@RequiredArgsConstructor
public class NotifyInfoTaskView implements NotifyTaskView {

    private final NotifyInfo entity;

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
        NotifyInfoDto request = JSON.parseObject(entity.getRequestData(), NotifyInfoDto.class);
        JSONObject bizData = new JSONObject();
        bizData.put("type", EventTypeEnum.CALLBACK.name());
        bizData.put("method", entity.getApiName());
        bizData.put("appKey", entity.getAppKey());
        bizData.put("version", entity.getApiVersion());
        bizData.put("timestamp", MdpBizMsgCrypt.generateTimestamp());
        bizData.put("callLogId", entity.getCallLogId());
        bizData.put("bizContent", request != null ? request.getBizParams() : null);
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
