package top.mddata.console.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知接收人 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "通知接收人Dto")
public class NoticeRecipientDto implements Serializable {

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
    private Long noticeId;

    /**
     * 接收人ID
     * 站内信专用
     */
    @NotNull(message = "请填写接收人ID")
    @Schema(description = "接收人ID")
    private Long userId;

    /**
     * 是否已读
     */
    @NotNull(message = "请填写是否已读")
    @Schema(description = "是否已读")
    private Boolean read;

    /**
     * 已读时间
     */
    @Schema(description = "已读时间")
    private LocalDateTime readTime;

}
