package top.mddata.console.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.console.message.enumeration.MsgChannelEnum;

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
@Schema(description = "邮件发送Dto")
public class MsgSendMailDto extends MsgSendDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接收人邮箱")
    private List<String> recipientList;

    /**
     * 构建API定时发送者
     *
     * @param scheduledSendTime 计划发送时间
     * @return this
     */
    public static MsgSendMailDto buildApiScheduledSender(LocalDateTime scheduledSendTime) {
        MsgSendMailDto dto = new MsgSendMailDto();
        dto.setChannel(MsgChannelEnum.API).setIsTiming(true).setScheduledSendTime(scheduledSendTime);
        return dto;
    }

    /**
     * 构建API发送者
     *
     * @return this
     */
    public static MsgSendMailDto buildApiSender() {
        MsgSendMailDto dto = new MsgSendMailDto();
        dto.setChannel(MsgChannelEnum.API).setIsTiming(false);
        return dto;
    }

    /**
     * 构建JOB发送者
     *
     * @return this
     */
    public static MsgSendMailDto buildJobSender() {
        MsgSendMailDto dto = new MsgSendMailDto();
        dto.setChannel(MsgChannelEnum.JOB).setIsTiming(false);
        return dto;
    }

    /**
     * 添加接收人
     *
     * @param email 接收人邮箱
     * @return this
     */
    public MsgSendMailDto addRecipient(String email) {
        if (this.recipientList == null) {
            this.recipientList = new ArrayList<>();
        }
        this.recipientList.add(email);
        return this;
    }
}
