package top.mddata.open.admin.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 回调任务实体类。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class NotifyInfoBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_notify_info";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属事件类型
     */
    private Long eventTypeId;

    /**
     * 事件类型
     */
    private String event;

    /**
     * 所属调用
     */
    private Long callLogId;

    /**
     * 通知类型
     * [0-推送 1-回调]
     */
    private Integer notifyType;

    /**
     * 所属应用
     */
    private Long appId;

    /**
     * 应用秘钥
     */
    private String appKey;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 接口版本
     */
    private String apiVersion;

    /**
     * 回调url
     */
    private String notifyUrl;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 最近一次请求时间
     */
    private LocalDateTime lastRequestTime;

    /**
     * 下一次请求时间
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
     * [0-待执行 1-执行成功,2-执行失败, 3-重试结束 4-手动结束]
     */
    private String execStatus;

    /**
     * 备注
     */
    private String remark;

}
