package top.mddata.console.query.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 请求日志 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2026-05-08 12:35:58
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Schema(description = "请求日志")
public class RequestLogDetailQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private String requestParam;

    /**
     * 响应体
     */
    @Schema(description = "响应体")
    private String responseBody;

    /**
     * 异常堆栈
     */
    @Schema(description = "异常堆栈")
    private String exceptionStack;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

}
