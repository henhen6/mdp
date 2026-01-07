package top.mddata.api.oepn.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import top.mddata.api.oepn.MsgOpenService;
import top.mddata.api.oepn.dto.SendMailDto;
import top.mddata.api.oepn.dto.SendNoticeDto;
import top.mddata.api.oepn.dto.SendSmsDto;
import top.mddata.console.message.dto.MsgSendMailDto;
import top.mddata.console.message.dto.MsgSendNoticeDto;
import top.mddata.console.message.dto.MsgSendSmsDto;
import top.mddata.console.message.enumeration.MsgChannelEnum;
import top.mddata.console.message.enumeration.MsgRecipientScopeEnum;
import top.mddata.console.message.facade.MsgFacade;

/**
 *
 * @author henhen
 * @since 2026/1/7 12:35
 */
@DubboService
@Service
@Slf4j
@RequiredArgsConstructor
public class MsgOpenServiceImpl implements MsgOpenService {
    private final MsgFacade msgFacade;

    @Override
    public void sendSmsByTemplateKey(SendSmsDto data) {
        MsgSendSmsDto dto = new MsgSendSmsDto();
        dto.setRecipientList(data.getRecipientList())
                .setChannel(MsgChannelEnum.API)
                .setTemplateKey(data.getTemplateKey())
                .setParamList(data.getParamList())
                .setIsTiming(data.getIsTiming())
                .setScheduledSendTime(data.getScheduledSendTime())
                .setBizId(data.getBizId())
                .setBizType(data.getBizType());
        msgFacade.sendByTemplateKey(dto);
    }

    @Override
    public void sendEmailByTemplateKey(SendMailDto data) {
        MsgSendMailDto dto = new MsgSendMailDto();
        dto.setRecipientList(data.getRecipientList())
                .setChannel(MsgChannelEnum.API)
                .setTemplateKey(data.getTemplateKey())
                .setParamList(data.getParamList())
                .setIsTiming(data.getIsTiming())
                .setScheduledSendTime(data.getScheduledSendTime())
                .setBizId(data.getBizId())
                .setBizType(data.getBizType());
        msgFacade.sendByTemplateKey(dto);
    }

    @Override
    public void sendNoticeByTemplateKey(SendNoticeDto data) {
        MsgSendNoticeDto dto = new MsgSendNoticeDto();
        dto.setAuthor(data.getAuthor())
                .setMsgCategory(MsgChannelEnum.of(data.getMsgCategory()))
                .setRecipientIdList(data.getRecipientIdList())
                .setUrl(data.getUrl())
                .setRecipientScope(MsgRecipientScopeEnum.of(data.getRecipientScope()))
                .setChannel(MsgChannelEnum.API)
                .setTemplateKey(data.getTemplateKey())
                .setParamList(data.getParamList())
                .setIsTiming(data.getIsTiming())
                .setScheduledSendTime(data.getScheduledSendTime())
                .setBizId(data.getBizId())
                .setBizType(data.getBizType());
        msgFacade.sendByTemplateKey(dto);
    }
}
