package top.mddata.open.admin.dto;

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
 * 回调日志 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "回调日志Dto")
public class NotifyLogDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "请填写主键", groups = BaseEntity.Update.class)
    @Schema(description = "主键")
    private Long id;

    /**
     * 所属回调
     */
    @NotNull(message = "请填写所属回调")
    @Schema(description = "所属回调")
    private Long notifyInfoId;

    /**
     * 请求时间
     */
    @Schema(description = "请求时间")
    private LocalDateTime requestTime;

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
     * 状态
     * [1-执行成功 2-执行失败]
     */
    @NotEmpty(message = "请填写状态")
    @Size(max = 1, message = "状态长度不能超过{max}")
    @Schema(description = "状态")
    private String execStatus;

    /**
     * 失败原因
     */
    @Size(max = 536870911, message = "失败原因长度不能超过{max}")
    @Schema(description = "失败原因")
    private String errorMsg;

}
