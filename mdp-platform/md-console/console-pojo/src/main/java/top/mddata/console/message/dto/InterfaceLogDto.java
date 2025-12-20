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
 * @since 2025-12-21 00:12:47
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "接口执行日志记录Dto")
public class InterfaceLogDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "请填写", groups = BaseEntity.Update.class)
    @Schema(description = "")
    private Long id;

    /**
     * 接口日志ID;
     * #extend_interface_log
     */
    @NotNull(message = "请填写接口日志ID;")
    @Schema(description = "接口日志ID;")
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
    private String params;

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
