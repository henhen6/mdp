package top.mddata.sdk.simple.api.msg;

import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.simple.request.msg.SendMailDto;

/**
 * 根据消息模板发送 站内信
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
public class SendMailApi extends BaseParam<SendMailDto, Void> {
    @Override
    protected String method() {
        return "msg.sendEmail";
    }
}