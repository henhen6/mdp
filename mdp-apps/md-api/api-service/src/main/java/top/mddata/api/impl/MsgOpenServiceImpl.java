package top.mddata.api.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import top.mddata.api.MsgOpenService;
import top.mddata.api.dto.SendMailDto;
import top.mddata.api.dto.SendNoticeDto;
import top.mddata.api.dto.SendSmsDto;
import top.mddata.console.dto.message.MsgSendMailDto;
import top.mddata.console.dto.message.MsgSendNoticeDto;
import top.mddata.console.dto.message.MsgSendSmsDto;
import top.mddata.console.enumeration.message.MsgCategoryEnum;
import top.mddata.console.enumeration.message.MsgChannelEnum;
import top.mddata.console.enumeration.message.MsgRecipientScopeEnum;
import top.mddata.console.facade.message.MsgFacade;

/**
 * 在使用微服务架构时，msgFacade 接口依赖于console-server。
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
                .setMsgCategory(MsgCategoryEnum.of(data.getMsgCategory()))
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
