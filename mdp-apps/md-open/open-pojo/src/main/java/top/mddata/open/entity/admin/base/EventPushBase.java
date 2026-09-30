package top.mddata.open.entity.admin.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件推送任务实体类。
 *
 * @author henhen6
 * @since 2026-01-12 21:28:36
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class EventPushBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_event_push";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件触发ID
     */
    private Long eventTriggerId;

    /**
     * 事件编码
     */
    private String eventCode;

    /**
     * 所属应用
     */
    private Long appId;

    /**
     * 应用Key
     */
    private String appKey;

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 加密模式
     * （冗余自 AppKeys，避免推送时再查库），[0-明文模式 1-兼容模式 2-安全模式]
     */
    private Integer notifyEncryptionType;

    /**
     * 签名校验令牌
     * （冗余自 AppKeys）
     */
    private String notifyToken;

    /**
     * AES加解密密钥
     * （冗余自 AppKeys）
     */
    private String notifyEncodingAesKey;

    /**
     * 请求参数
     */
    private String requestData;

    /**
     * 最近请求时间
     */
    private LocalDateTime lastRequestTime;

    /**
     * 下次请求时间
     */
    private LocalDateTime nextRequestTime;

    /**
     * 最大请求次数
     */
    private Integer maxRequestCnt;

    /**
     * 已请求次数
     */
    private Integer requestCnt;

    /**
     * 执行状态
     * [0-待执行 1-执行成功 2-执行失败 3-重试结束 4-手动结束]
     */
    private String execStatus;

    /**
     * 备注
     */
    private String remark;

}
