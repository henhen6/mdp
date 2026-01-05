package top.mddata.gateway.sop.service;

import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

/**
 * 序列化/反序列化
 *
 * @author 六如
 */
public interface Serde {

    String toJson(Object object);

    String toXml(Object object);

    Map<String, Object> parseJson(String json);

    default JSONObject parseObject(String json) {
        if (json == null) {
            return null;
        }
        Map<String, Object> jsonObj = parseJson(json);
        return jsonObj instanceof JSONObject ? (JSONObject) jsonObj : new JSONObject(jsonObj);
    }

}
