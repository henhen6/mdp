package top.mddata.console.message.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 接口执行日志记录实体类。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class InterfaceLogBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_interface_log";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接口ID
     */
    private Long interfaceStatId;

    /**
     * 执行开始时间
     */
    private LocalDateTime execStartTime;
    /**
     * 执行结束时间
     */
    private LocalDateTime execEndTime;

    /**
     * 执行状态
     * [1-初始化 2-成功 3-失败]
     */
    private Integer status;

    /**
     * 请求参数
     */
    private String param;

    /**
     * 接口返回
     */
    private String result;

    /**
     * 异常信息
     */
    private String errorMsg;

}
