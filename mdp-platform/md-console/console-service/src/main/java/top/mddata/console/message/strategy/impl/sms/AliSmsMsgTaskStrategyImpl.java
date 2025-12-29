package top.mddata.console.message.strategy.impl.sms;

import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.stereotype.Service;
import top.mddata.base.utils.StrPool;
import top.mddata.console.message.entity.InterfaceConfig;
import top.mddata.console.message.entity.MsgTask;
import top.mddata.console.message.entity.MsgTaskRecipient;
import top.mddata.console.message.entity.MsgTemplate;
import top.mddata.console.message.strategy.AbstractMsgTaskStrategy;
import top.mddata.console.message.strategy.dto.MsgResult;
import top.mddata.console.message.strategy.dto.MsgTaskParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 阿里 短信
 * @author henhen
 * @since 2025/12/29 10:36
 */
@Slf4j
@Service("aliSmsMsgTaskStrategyImpl")
@RequiredArgsConstructor
public class AliSmsMsgTaskStrategyImpl extends AbstractMsgTaskStrategy {
    @Override
    public MsgResult exec(MsgTaskParam msgParam) {
        validParam(msgParam);
        Map<String, String> propertyParam = msgParam.getPropertyParam();
        // 多个手机号用逗号分割
        List<MsgTaskRecipient> taskRecipientList = msgParam.getRecipientList();
        List<String> phoneNumberList = taskRecipientList.stream().map(MsgTaskRecipient::getRecipient).toList();
        String phoneNumbers = taskRecipientList.stream().map(MsgTaskRecipient::getRecipient).collect(Collectors.joining(StrPool.COMMA));

        if (propertyParam.get("debug") != null && Convert.toBool(propertyParam.get("debug"))) {
            log.info("已忽略短信发送: {}", JSON.toJSONString(phoneNumbers));
            return MsgResult.builder().result(true).build();
        }

        MsgTemplate msgTemplate = msgParam.getMsgTemplate();
        MsgTask msgTask = msgParam.getMsgTask();

        // 解析参数
        LinkedHashMap<String, String> params = parseParam(msgTask.getParam());
        String template = msgTemplate.getSmsTemplateId();

        InterfaceConfig interfaceConfig = msgParam.getInterfaceConfig();
        SmsBlend smsBlend = SmsFactory.getSmsBlend(interfaceConfig.getKey());
        SmsResponse smsResponse = smsBlend.massTexting(phoneNumberList, template, params);

        MsgResult msgResult = new MsgResult();
        msgResult.setResult(smsResponse);
        msgResult.setTitle(msgTask.getTitle());
        msgResult.setContent(template);
        return msgResult;
    }

    @Override
    public boolean isSuccess(MsgResult result) {
        if (result.getResult() instanceof Boolean res) {
            return res;
        }
        SmsResponse smsResponse = (SmsResponse) result.getResult();
        return smsResponse.isSuccess();
    }
}
