package top.mddata.console.message.strategy.impl.sms;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.chuanglan.config.ChuangLanConfig;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.stereotype.Service;
import top.mddata.base.model.Kv;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 创蓝短信发送接口
 *
 * @author henhen
 * @since 2025/12/29 00:40
 */
@Slf4j
@Service("clSmsMsgStrategyImpl")
@RequiredArgsConstructor
public class ClSmsMsgTaskStrategyImpl extends AbstractMsgTaskStrategy {
    private static final Pattern REG = Pattern.compile("[$][{][a-zA-Z\\d\\\\_$]+[}]");
    private static final String VAR = "{$var}";

    @Override
    public MsgResult exec(MsgTaskParam msgParam) {
        validParam(msgParam);

        Map<String, String> propertyParam = msgParam.getPropertyParam();
        // 多个手机号用逗号分割
        List<MsgTaskRecipient> taskRecipientList = msgParam.getRecipientList();
        String phoneNumbers = taskRecipientList.stream().map(MsgTaskRecipient::getRecipient).collect(Collectors.joining(StrPool.COMMA));

        ChuangLanConfig config = new ChuangLanConfig();
        BeanUtil.fillBeanWithMap(propertyParam, config, true);
        if (propertyParam.get("debug") != null && Convert.toBool(propertyParam.get("debug"))) {
            log.info("已忽略短信发送: {}", JSON.toJSONString(phoneNumbers));
            return MsgResult.builder().result(true).build();
        }
        MsgTemplate msgTemplate = msgParam.getMsgTemplate();
        MsgTask msgTask = msgParam.getMsgTask();
        // 短信签名：优先取模版中配置的签名
        String sign = StrUtil.isEmpty(msgTemplate.getSmsSign()) ? config.getSignature() : msgTemplate.getSmsSign();

        // 解析参数
        List<Kv> paramList = JSON.parseArray(msgTask.getParam(), Kv.class);
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        for (Kv kv : paramList) {
            params.put(kv.getKey(), kv.getValue());
        }
        String template = buildVariableContent(sign, msgTemplate.getContent());

        InterfaceConfig interfaceConfig = msgParam.getInterfaceConfig();
        SmsBlend smsBlend = SmsFactory.getSmsBlend(interfaceConfig.getKey());
        SmsResponse smsResponse = smsBlend.sendMessage(phoneNumbers, template, params);

        MsgResult msgResult = new MsgResult();
        msgResult.setResult(smsResponse);
        msgResult.setTitle(msgTask.getTitle());
        msgResult.setContent(template);
        return msgResult;
    }

    /**
     * 拼接签名和模板
     * @param sign 短信签名
     * @param content 创蓝认可的短信模版
     * @return 签名和模板
     */
    private String buildVariableContent(String sign, String content) {
        return StrUtil.format("【{}】{}", sign, replaceContent(content));
    }

    /**
     * 替换模版中的占位符
     *
     * ${xxx} 替换为 {$var}
     *
     * 系统中统一配置的变量格式为： ${xxx}
     * 创蓝短信配置的变量格式为： {$var}
     *
     * @param content 短信模板
     * @return 创蓝短信中实际配置的短信模版
     */
    private String replaceContent(String content) {
        if (StrUtil.isEmpty(content)) {
            return content;
        }
        Matcher m = REG.matcher(content);
        String result = content;
        while (m.find()) {
            String group = m.group();
            result = result.replace(group, VAR);
        }
        return result;
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
