package top.mddata.open.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件触发 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "事件触发")
public class EventTriggerDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 事件编码
     */
    @NotEmpty(message = "请填写事件编码")
    @Size(max = 255, message = "事件编码长度不能超过{max}")
    @Schema(description = "事件编码")
    private String eventCode;

    /**
     * 触发时间
     */
    @Schema(description = "触发时间")
    private LocalDateTime triggerAt;

    /**
     * 事件内容
     */
    @Size(max = 255, message = "事件内容长度不能超过{max}")
    @Schema(description = "事件内容")
    private String eventContent;

}
