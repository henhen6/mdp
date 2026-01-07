package top.mddata.api.oepn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
@Schema(description = "短信发送Dto")
public class SendSmsDto extends MsgSendDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接收人手机号")
    private List<String> recipientList;

    /**
     * 构建API定时发送者
     *
     * @param scheduledSendTime 计划发送时间
     * @return this
     */
    public static SendSmsDto buildApiScheduledSender(LocalDateTime scheduledSendTime) {
        SendSmsDto dto = new SendSmsDto();
        dto.setIsTiming(true).setScheduledSendTime(scheduledSendTime);
        return dto;
    }

    /**
     * 构建API发送者
     *
     * @return this
     */
    public static SendSmsDto buildApiSender() {
        SendSmsDto dto = new SendSmsDto();
        dto.setIsTiming(false);
        return dto;
    }

    /**
     * 添加接收人
     *
     * @param phone 接收人手机号
     * @return this
     */
    public SendSmsDto addRecipient(String phone) {
        if (this.recipientList == null) {
            this.recipientList = new ArrayList<>();
        }
        this.recipientList.add(phone);
        return this;
    }
}
