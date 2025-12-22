package top.mddata.console.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.console.message.enumeration.MsgChannelEnum;
import top.mddata.console.message.enumeration.MsgRecipientScopeEnum;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 根据模版编码发送消息任务
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "站内信发送Dto")
public abstract class MsgSendNoticeDto extends MsgSendDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 消息分类
     * 站内信专用
     * [1-待办 2-公告 3-预警]
     */
    @Schema(description = "消息分类")
    private MsgChannelEnum msgCategory;

    /**
     * 发送人
     */
    @Size(max = 255, message = "发送人长度不能超过{max}")
    @Schema(description = "发送人")
    private String author;

    @Schema(description = "接收人ID")
    private List<Long> recipientIdList;
    /**
     * 接收范围
     * [0-所有人 1-指定用户 2-指定角色 3-指定部门]
     */
    @NotNull(message = "请填写接收范围")
    @Schema(description = "接收范围")
    private MsgRecipientScopeEnum recipientScope;

    /**
     * 跳转地址
     */
    @Schema(description = "跳转地址")
    private String url;
}
