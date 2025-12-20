package top.mddata.console.message.dto;

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

/**
 * 任务接收人 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "任务接收人Dto")
public class MsgTaskRecipientDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 消息ID
     */
    @NotNull(message = "请填写消息ID")
    @Schema(description = "消息ID")
    private Long msgTaskId;

    /**
     * 接收人
     * 站内信-id 短信-手机号 邮件-邮箱
     */
    @NotEmpty(message = "请填写接收人")
    @Size(max = 255, message = "接收人长度不能超过{max}")
    @Schema(description = "接收人")
    private String recipient;

}
