package top.mddata.console.entity.system.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 请求日志实体类。
 *
 * @author henhen6
 * @since 2026-05-08 12:35:58
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RequestLogDetailBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_request_log_detail";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 响应体
     */
    private String responseBody;

    /**
     * 异常堆栈
     */
    private String exceptionStack;

}
