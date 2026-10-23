package top.mddata.console.service.message.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSON;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.constant.ConfigKey;
import top.mddata.common.constant.FileObjectType;
import top.mddata.console.dto.message.MsgSendDto;
import top.mddata.console.dto.message.MsgSendMailDto;
import top.mddata.console.dto.message.MsgSendNoticeDto;
import top.mddata.console.dto.message.MsgSendSmsDto;
import top.mddata.console.dto.message.MsgTaskDto;
import top.mddata.console.entity.message.MsgTask;
import top.mddata.console.entity.message.MsgTaskRecipient;
import top.mddata.console.entity.message.MsgTemplate;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.enumeration.message.MsgChannelEnum;
import top.mddata.console.enumeration.message.MsgRecipientScopeEnum;
import top.mddata.console.enumeration.message.MsgTaskStatusEnum;
import top.mddata.console.enumeration.message.MsgTypeEnum;
import top.mddata.console.mapper.message.MsgTaskMapper;
import top.mddata.console.service.message.MsgTaskRecipientService;
import top.mddata.console.service.message.MsgTaskService;
import top.mddata.console.service.message.MsgTemplateService;
import top.mddata.console.service.message.event.MsgSendEvent;
import top.mddata.console.service.message.event.dto.MsgSendEventDto;
import top.mddata.console.service.system.ConfigService;
import top.mddata.console.service.system.FileService;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    private final ConfigService configService; // 同一个服务，直接调用 service。跨服务需要调用 facade
    private final FileService fileService;

    @Override
    protected MsgTask saveBefore(Object save) {

        MsgTask entity = BeanUtil.toBean(save, MsgTask.class);
        entity.setId(null);
        entity.setType(MsgTypeEnum.NOTICE.getCode());
        entity.setStatus(MsgTaskStatusEnum.DRAFT.getCode());
        entity.setChannel(MsgChannelEnum.WEB.getCode());
        entity.setSenderId(ContextUtil.getUserId());
        entity.setOrgId(ContextUtil.getCurrentDeptOrCompanyId());

        return entity;
    }

    @Override
    protected void saveAfter(Object save, MsgTask entity) {
        // publish 流程以实体调 save()，此处入参不一定是 DTO，需守卫后再取扩展字段
        if (save instanceof MsgTaskDto dto) {
            relateContentFiles(dto, entity);
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
        relateContentFiles(dto, entity);
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

    /**
     * 关联内容引用的附件：contentFileIds 为 null 时（老客户端未携带）跳过，避免误删；
     * 空列表是有效指令（内容已无附件），清理该任务全部历史关联
     */
    private void relateContentFiles(MsgTaskDto dto, MsgTask entity) {
        if (dto.getContentFileIds() == null) {
            return;
        }
        fileService.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getId())
                .objectType(FileObjectType.Console.MSG_TASK_CONTENT)
                .build().addKeepFileIds(dto.getContentFileIds()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        boolean flag = super.removeByIds(idList);
        // 任务删除后清理收件人与内容附件，避免残留
        List<Long> taskIds = idList.stream().map(id -> Long.valueOf(String.valueOf(id))).toList();
        msgTaskRecipientService.removeByMsgTaskIds(taskIds);
        fileService.releaseFilesByBiz(FileObjectType.Console.MSG_TASK_CONTENT, taskIds);
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publish(MsgTaskDto data) {
        String msgTemplateKey = configService.getString(ConfigKey.Console.MESSAGE_NOTICE, null);  // 在【系统配置】获取站内信的模板标识  虽然多了一次查询，但方便系统运行过程中切换模版
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
        entity.setOrgId(ContextUtil.getCurrentDeptOrCompanyId());
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
        // publish 走 save/updateById 直调，不触发 saveAfter/updateAfter 钩子，附件关联需显式调用
        relateContentFiles(data, entity);

        if (data.getIsTiming() != null && data.getIsTiming() && data.getScheduledSendTime() != null) {
            log.info("定时发送消息任务：{}", entity);
            // 定时发送
        } else {
            // 执行发送
            MsgSendEventDto dto = new MsgSendEventDto();
            // 一定要调用copy方法写入线程参数
            dto.setMsgTaskId(entity.getId()).copy();
            SpringUtil.publishEvent(new MsgSendEvent(dto));
        }

        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendByTemplateKey(MsgSendDto data) {
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
        entity.setOrgId(ContextUtil.getCurrentDeptOrCompanyId());
        entity.setContent("");
        entity.setTitle("");

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
            log.info("定时发送消息任务：{}", entity);
        } else {
            // 执行发送
            MsgSendEventDto dto = new MsgSendEventDto();
            // 一定要调用copy方法写入线程参数
            dto.setMsgTaskId(entity.getId()).copy();
            SpringUtil.publishEvent(new MsgSendEvent(dto));
        }

        return entity.getId();
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
        ArgumentAssert.notNull(data.getChannel(), "请填写消息发送渠道");
        ArgumentAssert.contain(Arrays.asList(MsgChannelEnum.JOB.getCode(), MsgChannelEnum.API.getCode()), data.getChannel().getCode(), "非法的参数, 发送渠道: {}", data.getChannel().getDesc());
        return msgTemplate;
    }


}
