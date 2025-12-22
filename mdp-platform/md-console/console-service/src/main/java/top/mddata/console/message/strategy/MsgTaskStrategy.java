package top.mddata.console.message.strategy;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import top.mddata.base.model.Kv;
import top.mddata.base.utils.FreeMarkerUtil;
import top.mddata.console.message.entity.MsgTask;
import top.mddata.console.message.entity.MsgTemplate;
import top.mddata.console.message.enumeration.MsgChannelEnum;
import top.mddata.console.message.enumeration.MsgTypeEnum;
import top.mddata.console.message.glue.GlueFactory;
import top.mddata.console.message.strategy.dto.MsgResult;
import top.mddata.console.message.strategy.dto.MsgTaskParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息任务执行策略
 * @author henhen
 */
public interface MsgTaskStrategy {
    /**
     * 解析参数
     *
     * @param param param
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @author henhen
     */
    default Map<String, String> parseParam(String param) {
        Map<String, String> map = new LinkedHashMap<>();
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
    default MsgResult replaceVariable(MsgTask msgTask, MsgTemplate msgTemplate) {
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

    /**
     * 执行发送
     *
     * @param msgTaskParam 消息任务参数
     * @throws Exception 异常
     */
    MsgResult exec(MsgTaskParam msgTaskParam) throws Exception;

    /**
     * 是否执行成功
     *
     * @param result 执行函数的返回值
     * @return true表示成功
     */
    default boolean isSuccess(MsgResult result) {
        return true;
    }
}
