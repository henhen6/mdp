package top.mddata.console.message.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.ContextUtil;
import top.mddata.base.utils.SpringUtils;
import top.mddata.common.constant.ConfigKey;
import top.mddata.console.message.dto.MsgSendDto;
import top.mddata.console.message.dto.MsgSendMailDto;
import top.mddata.console.message.dto.MsgSendNoticeDto;
import top.mddata.console.message.dto.MsgSendSmsDto;
import top.mddata.console.message.dto.MsgTaskDto;
import top.mddata.console.message.entity.MsgTask;
import top.mddata.console.message.entity.MsgTaskRecipient;
import top.mddata.console.message.entity.MsgTemplate;
import top.mddata.console.message.enumeration.MsgChannelEnum;
import top.mddata.console.message.enumeration.MsgRecipientScopeEnum;
import top.mddata.console.message.enumeration.MsgTaskStatusEnum;
import top.mddata.console.message.enumeration.MsgTypeEnum;
import top.mddata.console.message.event.MsgSendEvent;
import top.mddata.console.message.event.dto.MsgSendEventDto;
import top.mddata.console.message.mapper.MsgTaskMapper;
import top.mddata.console.message.service.MsgTaskRecipientService;
import top.mddata.console.message.service.MsgTaskService;
import top.mddata.console.message.service.MsgTemplateService;
import top.mddata.console.system.facade.ConfigFacade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 消息任务 服务层实现。
 *
 * @author henhen6
 * @since 2025-12-21 00:02:22
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MsgTaskServiceImpl extends SuperServiceImpl<MsgTaskMapper, MsgTask> implements MsgTaskService {
    private final MsgTaskRecipientService msgTaskRecipientService;
    private final MsgTemplateService msgTemplateService;
    private final ConfigFacade configFacade;

    @Override
    protected MsgTask saveBefore(Object save) {

        MsgTask entity = BeanUtil.toBean(save, MsgTask.class);
        entity.setId(null);
        entity.setType(MsgTypeEnum.NOTICE.getCode());
        entity.setStatus(MsgTaskStatusEnum.DRAFT.getCode());
        entity.setChannel(MsgChannelEnum.WEB.getCode());
        entity.setSenderId(ContextUtil.getUserId());

        return entity;
    }

    @Override
    protected void saveAfter(Object save, MsgTask entity) {
        MsgTaskDto dto = (MsgTaskDto) save;
        List<Long> recipientIdList = dto.getRecipientList();
        if (CollUtil.isNotEmpty(recipientIdList)) {
            List<MsgTaskRecipient> recipientList = recipientIdList.stream().map(id -> {
                MsgTaskRecipient recipient = new MsgTaskRecipient();
                recipient.setMsgTaskId(entity.getId());
                recipient.setRecipient(String.valueOf(id));
                return recipient;
            }).toList();
            msgTaskRecipientService.saveBatch(recipientList);
        }
    }

    @Override
    protected MsgTask updateBefore(Object update) {
        MsgTask entity = UpdateEntity.of(MsgTask.class);
        BeanUtil.copyProperties(update, entity);
        entity.setType(MsgTypeEnum.NOTICE.getCode());
        entity.setStatus(MsgTaskStatusEnum.DRAFT.getCode());
        entity.setChannel(MsgChannelEnum.WEB.getCode());
        entity.setSenderId(ContextUtil.getUserId());
        return entity;
    }

    @Override
    protected void updateAfter(Object update, MsgTask entity) {
        MsgTaskDto dto = (MsgTaskDto) update;
        List<Long> recipientIdList = dto.getRecipientList();
        msgTaskRecipientService.removeByMsgTaskId(entity.getId());
        if (CollUtil.isNotEmpty(recipientIdList)) {
            List<MsgTaskRecipient> recipientList = recipientIdList.stream().map(id -> {
                MsgTaskRecipient recipient = new MsgTaskRecipient();
                recipient.setMsgTaskId(entity.getId());
                recipient.setRecipient(String.valueOf(id));
                return recipient;
            }).toList();
            msgTaskRecipientService.saveBatch(recipientList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publish(MsgTaskDto data) {
        String msgTemplateKey = configFacade.getString(ConfigKey.Console.MESSAGE_NOTICE, null);  // 在【系统配置】获取站内信的模板标识  虽然多了一次查询，但方便系统运行过程中切换模版
        ArgumentAssert.notEmpty(msgTemplateKey, "请联系管理员在【系统配置】页面配置【站内信】的模板标识");
        MsgTemplate defNoticeTemplate = msgTemplateService.getByTemplateKey(msgTemplateKey);   // 在【消息模板】获取站内信的模板id
        ArgumentAssert.notNull(defNoticeTemplate, "请联系管理员在【消息模板】页面配置【站内信】的消息模板");

        MsgTask entity;
        if (data.getId() == null) {
            entity = BeanUtil.toBean(data, MsgTask.class);
            entity.setId(null);
        } else {
            entity = UpdateEntity.of(MsgTask.class);
            BeanUtil.copyProperties(data, entity);
        }
        entity.setType(MsgTypeEnum.NOTICE.getCode());
        entity.setStatus(MsgTaskStatusEnum.WAITING.getCode());
        entity.setChannel(MsgChannelEnum.WEB.getCode());
        entity.setSenderId(ContextUtil.getUserId());
        entity.setTemplateId(defNoticeTemplate.getId());

        if (data.getId() == null) {
            this.save(entity);
        } else {
            this.updateById(entity);
            msgTaskRecipientService.removeByMsgTaskId(entity.getId());
        }
        List<Long> recipientIdList = data.getRecipientList();
        if (CollUtil.isNotEmpty(recipientIdList)) {
            List<MsgTaskRecipient> recipientList = recipientIdList.stream().map(id -> {
                MsgTaskRecipient recipient = new MsgTaskRecipient();
                recipient.setMsgTaskId(entity.getId());
                recipient.setRecipient(String.valueOf(id));
                return recipient;
            }).toList();
            msgTaskRecipientService.saveBatch(recipientList);
        }

        if (data.getIsTiming() != null && data.getIsTiming() && data.getScheduledSendTime() != null) {
            // 定时发送
        } else {
            // 执行发送
            MsgSendEventDto dto = new MsgSendEventDto();
            // 一定要调用copy方法写入线程参数
            dto.setMsgTaskId(entity.getId()).copy();
            SpringUtils.publishEvent(new MsgSendEvent(dto));
        }

        return true;
    }


    public Boolean sendByTemplateKey(MsgSendDto data) {
        MsgTemplate msgTemplate = validParam(data);
        MsgTask entity = new MsgTask();
        entity.setTemplateId(msgTemplate.getId());
        if (CollUtil.isNotEmpty(data.getParamList())) {
            entity.setParam(JSON.toJSONString(data.getParamList()));
        }
        entity.setIsTiming(data.getIsTiming());
        entity.setScheduledSendTime(data.getScheduledSendTime());
        entity.setBizId(data.getBizId());
        entity.setBizType(data.getBizType());
        entity.setChannel(data.getChannel() != null ? data.getChannel().getCode() : null);
        entity.setType(msgTemplate.getMsgType());
        entity.setStatus(MsgTaskStatusEnum.WAITING.getCode());
        entity.setSenderId(ContextUtil.getUserId());

        if (data instanceof MsgSendNoticeDto noticeDto) {
            entity.setMsgCategory(noticeDto.getMsgCategory() != null ? noticeDto.getMsgCategory().getCode() : null);
            entity.setAuthor(noticeDto.getAuthor());
            entity.setRecipientScope(noticeDto.getRecipientScope().getCode());
            entity.setUrl(noticeDto.getUrl());
        }
        this.save(entity);

        List<MsgTaskRecipient> recipientList = new ArrayList<>();
        if (data instanceof MsgSendSmsDto smsDto) {
            List<String> phoneList = smsDto.getRecipientList();
            if (CollUtil.isNotEmpty(phoneList)) {
                recipientList = phoneList.stream().map(phone -> {
                    MsgTaskRecipient recipient = new MsgTaskRecipient();
                    recipient.setMsgTaskId(entity.getId());
                    recipient.setRecipient(phone);
                    return recipient;
                }).toList();
            }
        } else if (data instanceof MsgSendMailDto mailDto) {
            List<String> emailList = mailDto.getRecipientList();
            if (CollUtil.isNotEmpty(emailList)) {
                recipientList = emailList.stream().map(email -> {
                    MsgTaskRecipient recipient = new MsgTaskRecipient();
                    recipient.setMsgTaskId(entity.getId());
                    recipient.setRecipient(email);
                    return recipient;
                }).toList();
            }
        } else if (data instanceof MsgSendNoticeDto noticeDto) {
            List<Long> idList = noticeDto.getRecipientIdList();
            if (CollUtil.isNotEmpty(idList)) {
                recipientList = idList.stream().map(id -> {
                    MsgTaskRecipient recipient = new MsgTaskRecipient();
                    recipient.setMsgTaskId(entity.getId());
                    recipient.setRecipient(String.valueOf(id));
                    return recipient;
                }).toList();
            }
        }
        if (CollUtil.isNotEmpty(recipientList)) {
            msgTaskRecipientService.saveBatch(recipientList);
        }

        if (data.getIsTiming() != null && data.getIsTiming() && data.getScheduledSendTime() != null) {
            // 定时发送
        } else {
            // 执行发送
            MsgSendEventDto dto = new MsgSendEventDto();
            // 一定要调用copy方法写入线程参数
            dto.setMsgTaskId(entity.getId()).copy();
            SpringUtils.publishEvent(new MsgSendEvent(dto));
        }
        return true;
    }

    /**
     * 验证数据，并初始化数据
     */
    private MsgTemplate validParam(MsgSendDto data) {
        ArgumentAssert.notEmpty(data.getTemplateKey(), "消息模板不能为空");

        MsgTemplate msgTemplate = null;
        if (StrUtil.isNotEmpty(data.getTemplateKey())) {
            msgTemplate = msgTemplateService.getByTemplateKey(data.getTemplateKey());
        }
        ArgumentAssert.notNull(msgTemplate, "消息模板不存在");

        //1，验证必要参数
        if (data instanceof MsgSendNoticeDto noticeDto) {
            if (!MsgRecipientScopeEnum.ALL.eq(noticeDto.getRecipientScope())) {
                ArgumentAssert.notEmpty(noticeDto.getRecipientIdList(), "请填写消息接收人ID");
            }
        } else if (data instanceof MsgSendMailDto mailDto) {
            ArgumentAssert.notEmpty(mailDto.getRecipientList(), "请填写消息接收人邮箱");
        } else if (data instanceof MsgSendSmsDto smsDto) {
            ArgumentAssert.notEmpty(smsDto.getRecipientList(), "请填写消息接收人手机号");
        }

        // 验证定时发送的时间，至少大于（当前时间+5分钟） ，是为了防止 定时调度或者是保存数据跟不上
        if (data.getIsTiming() != null && data.getIsTiming()) {
            boolean flag = LocalDateTime.now().plusMinutes(4).isBefore(data.getScheduledSendTime());
            ArgumentAssert.isTrue(flag, "定时发送时间至少在当前时间的5分钟之后");
        } else {
            data.setScheduledSendTime(null);
        }
        ArgumentAssert.contain(Arrays.asList(MsgChannelEnum.JOB.getCode(), MsgChannelEnum.API.getCode()), data.getChannel(), "非法的参数, 发送渠道: {}", data.getChannel());
        return msgTemplate;
    }


}
