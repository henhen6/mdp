package top.mddata.workbench.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知接收人 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-12-26 09:55:35
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知接收人Query")
public class NoticeRecipientQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
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
