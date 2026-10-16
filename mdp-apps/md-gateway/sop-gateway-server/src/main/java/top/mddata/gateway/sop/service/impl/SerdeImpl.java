package top.mddata.gateway.sop.service.impl;

import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.mddata.gateway.sop.service.Serde;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 六如
 */
@Service
public class SerdeImpl implements Serde {

    /**
     * remove specify class field refer to dubbo generic invoke
     */
    private static final SimplePropertyPreFilter CLASS_NAME_PRE_FILTER = new SimplePropertyPreFilter(HashMap.class);
    static JSONWriter.Context writeContext;

    static {
        CLASS_NAME_PRE_FILTER.getExcludes().add("class");
    }

    @Value("${mdp.gateway.serialize.date-format}")
    protected String dateFormat;

    @Override
    public String toJson(Object object) {
        return JSON.toJSONString(object, writeContext);
    }

    @Override
    public String toXml(Object object) {
        return XmlUtil.toStr(XmlUtil.beanToXml(object));
    }

    @Override
    public Map<String, Object> parseJson(String json) {
        return JSON.parseObject(json);
    }

    @PostConstruct
    public void init() {
        // Long 序列化为字符串：雪花 id 等大数超过 JS Number 安全范围（2^53），避免前端解析丢精度
        writeContext = new JSONWriter.Context(JSONWriter.Feature.WriteLongAsString);
        writeContext.setDateFormat(dateFormat);
        // 移除泛化调用返回class属性
        writeContext.setPropertyPreFilter(CLASS_NAME_PRE_FILTER);

        this.doInit();
    }

    protected void doInit() {

    }
}
