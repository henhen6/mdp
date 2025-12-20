package top.mddata.console.message.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.console.message.entity.base.NoticeRecipientBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知接收人 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "通知接收人Vo")
@Table(NoticeRecipientBase.TABLE_NAME)
public class NoticeRecipientVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private Long id;

    /**
     * 消息ID
     */
    @Schema(description = "消息ID")
    private Long noticeId;

    /**
     * 接收人ID
     * 站内信专用
     */
    @Schema(description = "接收人ID")
    private Long userId;

    /**
     * 是否已读
     */
    @Schema(description = "是否已读")
    private Boolean read;

    /**
     * 已读时间
     */
    @Schema(description = "已读时间")
    private LocalDateTime readTime;

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

    /**
     * 最后修改人
     */
    @Schema(description = "最后修改人")
    private Long updatedBy;

    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间")
    private LocalDateTime updatedAt;

}
