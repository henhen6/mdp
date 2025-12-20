package top.mddata.console.message.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务接收人实体类。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class MsgTaskRecipientBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_msg_task_recipient";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long msgTaskId;

    /**
     * 接收人
     * 站内信-id 短信-手机号 邮件-邮箱
     */
    private String recipient;

}
