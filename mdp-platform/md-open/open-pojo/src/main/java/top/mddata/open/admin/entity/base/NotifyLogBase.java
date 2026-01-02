package top.mddata.open.admin.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 回调日志实体类。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class NotifyLogBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_notify_log";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属回调
     */
    private Long notifyInfoId;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 响应内容
     */
    private String responseData;

    /**
     * 响应时间
     */
    private LocalDateTime responseTime;

    /**
     * 状态
     * [1-执行成功 2-执行失败]
     */
    private String execStatus;

    /**
     * 失败原因
     */
    private String errorMsg;

}
