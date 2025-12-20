package top.mddata.console.message.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知接收人实体类。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class NoticeRecipientBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_notice_recipient";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long noticeId;

    /**
     * 接收人ID
     * 站内信专用
     */
    private Long userId;

    /**
     * 是否已读
     */
    private Boolean read;

    /**
     * 已读时间
     */
    private LocalDateTime readTime;

}
