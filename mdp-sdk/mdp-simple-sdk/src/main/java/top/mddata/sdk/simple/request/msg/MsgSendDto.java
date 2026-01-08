package top.mddata.sdk.simple.request.msg;

import top.mddata.sdk.core.request.Kv;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 根据模版编码发送消息任务
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
public abstract class MsgSendDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板标识
     */
    private String templateKey;
    /**
     * 参数;
     * <p>
     * 需要封装为[{{‘key’:'', ’value’:''}, {'key2':'', 'value2':''}]格式
     */

    private List<Kv> paramList;

    /**
     * 是否定时发送
     */

    private Boolean isTiming;

    /**
     * 计划发送时间
     */

    private LocalDateTime scheduledSendTime;
    /**
     * 业务ID
     */

    private Long bizId;
    /**
     * 业务类型
     */


    private String bizType;

    /**
     * 添加参数
     *
     * @param key   参数名
     * @param value 参数值
     * @return this
     */
    public <T extends MsgSendDto> T addParam(String key, String value) {
        if (paramList == null) {
            paramList = new ArrayList<>();
        }
        paramList.add(new Kv(key, value));
        return (T) this;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public MsgSendDto setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
        return this;
    }

    public List<Kv> getParamList() {
        return paramList;
    }

    public MsgSendDto setParamList(List<Kv> paramList) {
        this.paramList = paramList;
        return this;
    }

    public Boolean getTiming() {
        return isTiming;
    }

    public MsgSendDto setTiming(Boolean timing) {
        isTiming = timing;
        return this;
    }

    public LocalDateTime getScheduledSendTime() {
        return scheduledSendTime;
    }

    public MsgSendDto setScheduledSendTime(LocalDateTime scheduledSendTime) {
        this.scheduledSendTime = scheduledSendTime;
        return this;
    }

    public Long getBizId() {
        return bizId;
    }

    public MsgSendDto setBizId(Long bizId) {
        this.bizId = bizId;
        return this;
    }

    public String getBizType() {
        return bizType;
    }

    public MsgSendDto setBizType(String bizType) {
        this.bizType = bizType;
        return this;
    }
}
