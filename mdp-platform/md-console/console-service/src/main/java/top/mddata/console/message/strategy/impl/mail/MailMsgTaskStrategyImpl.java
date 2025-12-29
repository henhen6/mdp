package top.mddata.console.message.strategy.impl.mail;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.email.jakarta.api.MailClient;
import org.dromara.email.jakarta.comm.config.MailSmtpConfig;
import org.dromara.email.jakarta.comm.entity.MailMessage;
import org.dromara.email.jakarta.core.factory.MailFactory;
import org.springframework.stereotype.Service;
import top.mddata.base.utils.StrPool;
import top.mddata.common.properties.MsgProperties;
import top.mddata.console.message.entity.MsgTask;
import top.mddata.console.message.entity.MsgTaskRecipient;
import top.mddata.console.message.entity.MsgTemplate;
import top.mddata.console.message.strategy.AbstractMsgTaskStrategy;
import top.mddata.console.message.strategy.dto.MsgResult;
import top.mddata.console.message.strategy.dto.MsgTaskParam;
import top.mddata.console.message.strategy.dto.mail.MailProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件
 *
 * @author henhen
 * @date 2025年12月28日23:54:34
 */
@Slf4j
@Service("mailMsgStrategyImpl")
@RequiredArgsConstructor
public class MailMsgTaskStrategyImpl extends AbstractMsgTaskStrategy {
    private final MsgProperties msgProperties;
    private final Map<String, MailClient> CLIENT_MAP = new HashMap<>();

    private MailClient getClient(String interfaceConfigId, Map<String, String> propertyParam, MailProperty property) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("interfaceConfigId").append(StrPool.EQUALS).append(interfaceConfigId);
        for (Map.Entry<String, String> entry : propertyParam.entrySet()) {
            keyBuilder.append(StrPool.AMPERSAND).append(entry.getKey()).append(StrPool.EQUALS).append(entry.getValue());
        }
        String key = keyBuilder.toString();

        // 任何一个参数修改了，都需要重新创建 MailClient
        if (CLIENT_MAP.containsKey(key)) {
            return CLIENT_MAP.get(key);
        }

        MailSmtpConfig config = MailSmtpConfig.builder()
                .port(property.getPort()).isSSL(property.getIsSsl()).smtpServer(property.getSmtpServer()).isAuth(property.getIsAuth())
                .fromAddress(property.getFromAddress()).nickName(property.getNickName())
                .username(property.getUsername()).password(property.getPassword())
                .build();
        //这里的key可以是任何可对比类型，用于后续从工厂取出邮件实现类用
        MailFactory.put(interfaceConfigId, config);
        MailClient mailClient = MailFactory.createMailClient(interfaceConfigId);
        CLIENT_MAP.put(key, mailClient);
        return mailClient;
    }

    @Override
    public MsgResult exec(MsgTaskParam msgParam) {
        validParam(msgParam);

        MsgTask msgTask = msgParam.getMsgTask();
        MsgTemplate msgTemplate = msgParam.getMsgTemplate();
        Map<String, String> propertyParam = msgParam.getPropertyParam();
        List<MsgTaskRecipient> taskRecipientList = msgParam.getRecipientList();
        MsgResult msgResult = replaceVariable(msgTask, msgTemplate, msgProperties);
        List<String> emails = taskRecipientList.stream().map(MsgTaskRecipient::getRecipient).toList();

        MailProperty property = new MailProperty();
        BeanUtil.fillBeanWithMap(propertyParam, property, true);
        if (property.getDebug() != null && property.getDebug()) {
            log.info("已忽略邮件发送: {}", JSON.toJSONString(emails));
            return MsgResult.builder().result(true).build();
        }

        MailMessage message = MailMessage.Builder()
                .mailAddress(emails) // 收件人地址
                .title(msgResult.getTitle())
                .htmlContent(msgResult.getContent())
                .build();

        MailClient mailClient = getClient(String.valueOf(msgTemplate.getInterfaceConfigId()), propertyParam, property);
        mailClient.send(message);

        log.info("已发送邮件: {}", JSON.toJSONString(emails));
        msgResult.setResult(true);
        return msgResult;
    }

    @Override
    public boolean isSuccess(MsgResult result) {
        return (boolean) result.getResult();
    }
}
