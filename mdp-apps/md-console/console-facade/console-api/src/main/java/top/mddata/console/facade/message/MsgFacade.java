package top.mddata.console.facade.message;

import top.mddata.console.dto.message.MsgSendDto;

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
     * @return 消息任务ID
     */
    Long sendByTemplateKey(MsgSendDto data);
}
