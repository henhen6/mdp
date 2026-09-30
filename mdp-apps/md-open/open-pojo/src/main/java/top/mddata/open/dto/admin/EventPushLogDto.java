package top.mddata.open.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件推送日志 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "事件推送日志")
public class EventPushLogDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "请填写主键", groups = BaseEntity.Update.class)
    @Schema(description = "主键")
    private Long id;

    /**
     * 所属推送
     */
    @NotNull(message = "请填写所属推送")
    @Schema(description = "所属推送")
    private Long eventPushId;

    /**
     * 请求时间
     */
    @Schema(description = "请求时间")
    private LocalDateTime requestTime;

    /**
     * 请求参数
     */
    @Size(max = 536870911, message = "请求参数长度不能超过{max}")
    @Schema(description = "请求参数")
    private String requestData;

    /**
     * 响应内容
     */
    @Size(max = 536870911, message = "响应内容长度不能超过{max}")
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
    @NotEmpty(message = "请填写执行状态")
    @Size(max = 1, message = "执行状态长度不能超过{max}")
    @Schema(description = "执行状态")
    private String execStatus;

    /**
     * 失败原因
     */
    @Size(max = 536870911, message = "失败原因长度不能超过{max}")
    @Schema(description = "失败原因")
    private String errorMsg;

}
