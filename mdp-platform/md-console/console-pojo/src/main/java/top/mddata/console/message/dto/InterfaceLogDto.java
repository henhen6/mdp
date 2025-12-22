package top.mddata.console.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
 * 接口执行日志记录 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "接口执行日志记录Dto")
public class InterfaceLogDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "请填写id", groups = BaseEntity.Update.class)
    @Schema(description = "id")
    private Long id;

    /**
     * 接口ID
     */
    @NotNull(message = "请填写接口ID")
    @Schema(description = "接口ID")
    private Long interfaceStatId;

    /**
     * 执行时间
     */
    @NotNull(message = "请填写执行时间")
    @Schema(description = "执行时间")
    private LocalDateTime execTime;

    /**
     * 执行状态
     * [1-初始化 2-成功 3-失败]
     */
    @Schema(description = "执行状态")
    private Integer status;

    /**
     * 请求参数
     */
    @Size(max = 536870911, message = "请求参数长度不能超过{max}")
    @Schema(description = "请求参数")
    private String param;

    /**
     * 接口返回
     */
    @Size(max = 536870911, message = "接口返回长度不能超过{max}")
    @Schema(description = "接口返回")
    private String result;

    /**
     * 异常信息
     */
    @Size(max = 536870911, message = "异常信息长度不能超过{max}")
    @Schema(description = "异常信息")
    private String errorMsg;

}
