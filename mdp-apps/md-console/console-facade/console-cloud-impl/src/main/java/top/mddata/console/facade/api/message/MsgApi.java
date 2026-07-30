package top.mddata.console.facade.api.message;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.console.dto.message.MsgSendDto;
import top.mddata.console.facade.api.message.fallback.MsgApiFallback;

/**
 *
 * @author henhen
 * @since 2026/5/10 23:07
 */
@FeignClient(name = AppConstants.CONSOLE_SERVER, fallback = MsgApiFallback.class)
public interface MsgApi {
    /**
     * 根据消息模板发送消息
     *         MsgSendMailDto 邮件消息参数
     *         MsgSendNoticeDto 站内信参数
     *         sgSendSmsDto 短信发送参数
     *
     * @param data 消息参数
     */
    @PostMapping("/message/msgTask/sendByTemplateKey")
    R<Long> sendByTemplateKey(@RequestBody MsgSendDto data);
}
