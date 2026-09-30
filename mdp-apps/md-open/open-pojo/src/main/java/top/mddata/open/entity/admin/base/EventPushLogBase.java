package top.mddata.open.entity.admin.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件推送日志实体类。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class EventPushLogBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_event_push_log";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属推送
     */
    private Long eventPushId;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 请求参数
     */
    private String requestData;

    /**
     * 响应内容
     */
    private String responseData;

    /**
     * 响应时间
     */
    private LocalDateTime responseTime;

    /**
     * 执行状态
     * [1-执行成功 2-执行失败]
     */
    private String execStatus;

    /**
     * 失败原因
     */
    private String errorMsg;

}
