package top.mddata.console.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 请求日志 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2026-05-08 12:35:58
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "请求日志")
public class RequestLogDetailDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "请填写主键", groups = BaseEntity.Update.class)
    @Schema(description = "主键")
    private Long id;

    /**
     * 请求参数
     */
    @Size(max = 536870911, message = "请求参数长度不能超过{max}")
    @Schema(description = "请求参数")
    private String requestParam;

    /**
     * 响应体
     */
    @Size(max = 536870911, message = "响应体长度不能超过{max}")
    @Schema(description = "响应体")
    private String responseBody;

    /**
     * 异常堆栈
     */
    @Size(max = 536870911, message = "异常堆栈长度不能超过{max}")
    @Schema(description = "异常堆栈")
    private String exceptionStack;

}
