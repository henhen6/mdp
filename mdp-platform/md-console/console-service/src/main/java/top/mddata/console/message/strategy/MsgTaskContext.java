package top.mddata.console.message.strategy;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.exception.BizException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.SpringUtils;
import top.mddata.console.message.dto.InterfaceConfigJsonDto;
import top.mddata.console.message.entity.InterfaceConfig;
import top.mddata.console.message.entity.InterfaceLog;
import top.mddata.console.message.entity.InterfaceStat;
import top.mddata.console.message.entity.MsgTask;
import top.mddata.console.message.entity.MsgTaskRecipient;
import top.mddata.console.message.entity.MsgTemplate;
import top.mddata.console.message.enumeration.InterfaceExecModeEnum;
import top.mddata.console.message.enumeration.MsgInterfaceLogStatusEnum;
import top.mddata.console.message.enumeration.MsgTaskStatusEnum;
import top.mddata.console.message.glue.GlueFactory;
import top.mddata.console.message.service.InterfaceConfigService;
import top.mddata.console.message.service.InterfaceLogService;
import top.mddata.console.message.service.InterfaceStatService;
import top.mddata.console.message.service.MsgTaskRecipientService;
import top.mddata.console.message.service.MsgTaskService;
import top.mddata.console.message.service.MsgTemplateService;
import top.mddata.console.message.strategy.dto.MsgResult;
import top.mddata.console.message.strategy.dto.MsgTaskParam;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息任务执行策略上下文
 *
 * @author henhen
 * @since 2025/12/21 23:11
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MsgTaskContext {
    private final MsgTaskService msgTaskService;
    private final MsgTaskRecipientService msgTaskRecipientService;
    private final MsgTemplateService msgTemplateService;
    private final InterfaceConfigService interfaceConfigService;
    private final InterfaceStatService interfaceStatService;
    private final InterfaceLogService interfaceLogService;

    /**
     * 根据消息任务发送消息
     * @param msgTaskId 消息任务ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void execSend(Long msgTaskId) {
        MsgTask msgTask = msgTaskService.getById(msgTaskId);
        ArgumentAssert.notNull(msgTask, "消息任务不存在");

        MsgTemplate msgTemplate = msgTemplateService.getById(msgTask.getTemplateId());
        ArgumentAssert.notNull(msgTemplate, "请配置消息模板");

        InterfaceConfig interfaceConfig = interfaceConfigService.getById(msgTemplate.getInterfaceConfigId());
        ArgumentAssert.notNull(interfaceConfig, "请配置消息模板【{}:{}】的接口", msgTemplate.getKey(), msgTemplate.getName());
        String configJson = interfaceConfig.getConfigJson();
        try {
            ArgumentAssert.isTrue(JSON.isValidArray(configJson), "消息模板【{}:{}】的接口配置信息错误： {}", msgTemplate.getKey(), msgTemplate.getName(), configJson);
        } catch (Exception e) {
            log.error("消息模板【{}:{}】的接口配置信息错误： {}", msgTemplate.getKey(), msgTemplate.getName(), configJson, e);
            throw new BizException(StrUtil.format("消息模板【{}:{}】的接口配置信息错误： {}", msgTemplate.getKey(), msgTemplate.getName(), configJson), e);
        }
        List<InterfaceConfigJsonDto> interfaceConfigList = JSON.parseArray(configJson, InterfaceConfigJsonDto.class);
        Map<String, String> propertyParam = new LinkedHashMap<>();
        interfaceConfigList.forEach(item -> propertyParam.put(item.getKey(), item.getValue()));

        // 任务接收人
        List<MsgTaskRecipient> msgTaskRecipientList = msgTaskRecipientService.listByMsgTaskId(msgTaskId);

        InterfaceStat interfaceStat = interfaceStatService.getById(interfaceConfig.getId());
        if (interfaceStat == null) {
            // 这里一般不会执行，因为：新增和删除 InterfaceConfig 时，会同时新增和删除 InterfaceStat
            interfaceStat = new InterfaceStat();
            interfaceStat.setId(interfaceConfig.getId());
            interfaceStat.setName(interfaceConfig.getName());
            interfaceStat.setFailCount(0);
            interfaceStat.setSuccessCount(0);
            interfaceStatService.save(interfaceStat);
        }

        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog
                .setInterfaceStatId(interfaceStat.getId())
                .setStatus(MsgInterfaceLogStatusEnum.INIT.getCode())
                .setExecStartTime(LocalDateTime.now())
                .setParam(msgTask.getParam());

        MsgTaskParam msgTaskParam = MsgTaskParam.builder().msgTask(msgTask).msgTemplate(msgTemplate)
                .propertyParam(propertyParam).recipientList(msgTaskRecipientList)
                .build();

        MsgTask msgTaskUpdate = UpdateEntity.of(MsgTask.class, msgTaskId);
        try {

            MsgResult result;
            MsgTaskStrategy msgTaskStrategy;
            // TODO 扩展magic-api
            if (InterfaceExecModeEnum.IMPL_CLASS.eq(interfaceConfig.getExecMode())) {
                // 实现类
                String implClass = interfaceConfig.getImplClass();
                msgTaskStrategy = SpringUtils.getBean(implClass, MsgTaskStrategy.class);
                ArgumentAssert.notNull(msgTaskStrategy, "实现类[{}]不存在", implClass);
                result = msgTaskStrategy.exec(msgTaskParam);
            } else {
                /*
                 * 注意： 脚本中，不支持lombok注解
                 */
                msgTaskStrategy = GlueFactory.getInstance().loadNewInstance(interfaceConfig.getScript());
                ArgumentAssert.notNull(msgTaskStrategy, "实现类不存在");
                result = msgTaskStrategy.exec(msgTaskParam);
            }

            boolean success = msgTaskStrategy.isSuccess(result);
            if (success) {
                interfaceLog.setStatus(MsgInterfaceLogStatusEnum.SUCCESS.getCode());
                msgTaskUpdate.setStatus(MsgTaskStatusEnum.SUCCESS.getCode());
                interfaceStatService.incrSuccessCount(interfaceStat.getId());
            } else {
                msgTaskUpdate.setStatus(MsgTaskStatusEnum.FAIL.getCode());
                interfaceLog.setStatus(MsgInterfaceLogStatusEnum.FAIL.getCode());
                interfaceStatService.incrFailCount(interfaceStat.getId());
            }

            interfaceLog.setResult(JSON.toJSONString(result));

            msgTaskUpdate.setTitle(result.getTitle());
            msgTaskUpdate.setContent(result.getContent());
            msgTaskService.updateById(msgTaskUpdate);
        } catch (Exception e) {
            msgTaskUpdate.setStatus(MsgTaskStatusEnum.FAIL.getCode());
            msgTaskService.updateById(msgTaskUpdate);

            log.error("执行发送消息失败", e);
            interfaceLog.setStatus(MsgInterfaceLogStatusEnum.FAIL.getCode());
            interfaceLog.setErrorMsg(ExceptionUtil.getRootCauseMessage(e));
            interfaceStatService.incrFailCount(interfaceStat.getId());

        } finally {
            interfaceLogService.save(interfaceLog);
        }

    }
}
