package top.mddata.api;

import com.gitee.sop.support.annotation.Open;
import top.mddata.api.dto.SendMailDto;
import top.mddata.api.dto.SendNoticeDto;
import top.mddata.api.dto.SendSmsDto;

/**
 * 消息数据接口
 * @author henhen
 * @since 2026/1/7 11:14
 */
public interface MsgOpenService {
    /**
     * 根据消息模板发送 短信
     *
     * @param data 消息参数
     */
    @Open(value = "msg.sendSms", description = "根据消息模板发送 短信")
    void sendSmsByTemplateKey(SendSmsDto data);

    /**
     * 根据消息模板发送 邮件
     *
     * @param data 消息参数
     */
    @Open(value = "msg.sendEmail", description = "根据消息模板发送 邮件")
    void sendEmailByTemplateKey(SendMailDto data);

    /**
     * 根据消息模板发送 站内信
     *
     * @param data 消息参数
     */
    @Open(value = "msg.sendNotice", description = "根据消息模板发送 站内信")
    void sendNoticeByTemplateKey(SendNoticeDto data);
}
