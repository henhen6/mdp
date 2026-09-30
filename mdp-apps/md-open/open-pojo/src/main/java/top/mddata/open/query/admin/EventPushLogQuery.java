package top.mddata.open.query.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件推送日志 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Schema(description = "事件推送日志")
public class EventPushLogQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 所属推送
     */
    @Schema(description = "所属推送")
    @NotNull(message = "所属推送不能为空")
    private Long eventPushId;

    /**
     * 请求时间
     */
    @Schema(description = "请求时间")
    private LocalDateTime requestTime;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private String requestData;

    /**
     * 响应内容
     */
    @Schema(description = "响应内容")
    private String responseData;

    /**
     * 响应时间
     */
    @Schema(description = "响应时间")
    private LocalDateTime responseTime;

    /**
     * 执行状态
     * [1-执行成功 2-执行失败]
     */
    @Schema(description = "执行状态")
    private String execStatus;

    /**
     * 失败原因
     */
    @Schema(description = "失败原因")
    private String errorMsg;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

}
