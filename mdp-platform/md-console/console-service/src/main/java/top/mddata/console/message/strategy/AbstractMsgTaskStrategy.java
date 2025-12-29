package top.mddata.console.message.strategy;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import top.mddata.base.model.Kv;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.FreeMarkerUtil;
import top.mddata.common.properties.MsgProperties;
import top.mddata.console.message.entity.InterfaceConfig;
import top.mddata.console.message.entity.MsgTask;
import top.mddata.console.message.entity.MsgTaskRecipient;
import top.mddata.console.message.entity.MsgTemplate;
import top.mddata.console.message.enumeration.MsgChannelEnum;
import top.mddata.console.message.enumeration.MsgTypeEnum;
import top.mddata.console.message.glue.GlueFactory;
import top.mddata.console.message.strategy.dto.MsgResult;
import top.mddata.console.message.strategy.dto.MsgTaskParam;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象类
 * @author henhen
 * @since 2025/12/29 10:18
 */
public abstract class AbstractMsgTaskStrategy implements MsgTaskStrategy {

    /**
     * 解析参数
     *
     * @param param param
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @author henhen
     */
    public LinkedHashMap<String, String> parseParam(String param) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (StrUtil.isNotEmpty(param)) {
            List<Kv> list = JSON.parseArray(param, Kv.class);
            for (Kv kv : list) {
                map.put(kv.getKey(), kv.getValue());
            }
        }
        return map;
    }

    /**
     * 替换变量
     *
     * @param msgTask          消息任务
     * @param msgTemplate 消息模板
     */
    public MsgResult replaceVariable(MsgTask msgTask, MsgTemplate msgTemplate, MsgProperties msgProperties) {
        if (MsgChannelEnum.WEB.eq(msgTask.getChannel()) && MsgTypeEnum.NOTICE.eq(msgTask.getType())) {
            return MsgResult.builder().title(msgTask.getTitle()).content(msgTask.getContent()).build();
        }
        String script = msgTemplate.getScript();
        String templateContent = msgTemplate.getContent();
        String templateTitle = msgTemplate.getTitle();
        Map<String, Object> params = new LinkedHashMap<>();
        if (StrUtil.isNotEmpty(msgTask.getParam())) {
            List<Kv> list = JSON.parseArray(msgTask.getParam(), Kv.class);
            for (Kv kv : list) {
                params.put(kv.getKey(), kv.getValue());
            }
        }

        // 内置系统参数
        params.put("_mdp", msgProperties.getParam() != null ? msgProperties.getParam() : new HashMap<>());
        Map<String, Object> resultParams = params;
        String title = templateTitle;
        String content = templateContent;
        if (StrUtil.isNotEmpty(script)) {
            resultParams = (Map<String, Object>) GlueFactory.getInstance().exeGroovyScript(script, params);
        }
        if (StrUtil.isNotEmpty(templateTitle)) {
            title = FreeMarkerUtil.generateString(templateTitle, resultParams);
        }
        if (StrUtil.isNotEmpty(templateContent)) {
            content = FreeMarkerUtil.generateString(templateContent, resultParams);
        }

        return MsgResult.builder().title(title).content(content).build();
    }

    public void validParam(MsgTaskParam msgTaskParam) {
        MsgTask msgTask = msgTaskParam.getMsgTask();
        MsgTemplate msgTemplate = msgTaskParam.getMsgTemplate();
        List<MsgTaskRecipient> recipientList = msgTaskParam.getRecipientList();
        InterfaceConfig interfaceConfig = msgTaskParam.getInterfaceConfig();
        Map<String, String> propertyParam = msgTaskParam.getPropertyParam();

        ArgumentAssert.notNull(msgTask, "消息任务为空");
        ArgumentAssert.notNull(msgTemplate, "消息模板为空");
        ArgumentAssert.notNull(recipientList, "接收人为空");
        ArgumentAssert.notNull(interfaceConfig, "模版接口为空");
        ArgumentAssert.isTrue(interfaceConfig.getState(), "模版接口已禁用");
        ArgumentAssert.notNull(propertyParam, "接口配置为空");
    }
}
