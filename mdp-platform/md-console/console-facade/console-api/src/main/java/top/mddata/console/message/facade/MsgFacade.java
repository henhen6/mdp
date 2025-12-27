package top.mddata.console.message.facade;

import top.mddata.console.message.dto.MsgSendDto;

/**
 * 消息发送服务
 * @author henhen6
 * @since 2025/12/27 14:04
 */
public interface MsgFacade {
    /**
     * 根据消息模板发送消息
     *         MsgSendMailDto 邮件消息参数
     *         MsgSendNoticeDto 站内信参数
     *         sgSendSmsDto 短信发送参数
     *
     * @param data 消息参数
     */
    void sendByTemplateKey(MsgSendDto data);
}
