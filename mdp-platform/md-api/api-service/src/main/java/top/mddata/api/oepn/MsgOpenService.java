package top.mddata.api.oepn;

import com.gitee.sop.support.annotation.Open;
import top.mddata.api.oepn.dto.SendMailDto;
import top.mddata.api.oepn.dto.SendNoticeDto;
import top.mddata.api.oepn.dto.SendSmsDto;

/**
 * 消息 数据接口
 * @author henhen
 * @since 2026/1/7 11:14
 */
public interface MsgOpenService {
    /**
     * 根据消息模板发送 短信
     *
     * @param data 消息参数
     */
    @Open("msg.sendSms")
    void sendSmsByTemplateKey(SendSmsDto data);

    /**
     * 根据消息模板发送 邮件
     *
     * @param data 消息参数
     */
    @Open("msg.sendEmail")
    void sendEmailByTemplateKey(SendMailDto data);

    /**
     * 根据消息模板发送 站内信
     *
     * @param data 消息参数
     */
    @Open("msg.sendNotice")
    void sendNoticeByTemplateKey(SendNoticeDto data);
}
