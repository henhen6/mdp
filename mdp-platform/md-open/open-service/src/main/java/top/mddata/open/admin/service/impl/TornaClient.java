package top.mddata.open.admin.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;
import top.mddata.base.exception.BizException;
import top.mddata.common.constant.ConfigKey;
import top.mddata.console.system.facade.ConfigFacade;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * torna服务 同步数据
 *
 * @author henhen6
 */
@Component
@RequiredArgsConstructor
public class TornaClient {
    private final ConfigFacade configFacade;

    public <T> T execute(String name, Object param, String token, Class<T> respClass) {
        JSONObject data = request(name, param, token).getJSONObject("data");
        return data.toJavaObject(respClass);
    }

    public <T> List<T> executeList(String name, Object param, String token, Class<T> respClass) {
        JSONArray data = request(name, param, token).getJSONArray("data");
        return data.toList(respClass);
    }

    private JSONObject request(String name, Object param, String token) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("access_token", token);
        if (param != null) {
            String json = JSON.toJSONString(param);
            params.put("data", UriUtils.encode(json, StandardCharsets.UTF_8));
        }

        String body = HttpUtil.post(getTornaApiUrl(), JSON.toJSONString(params));

        JSONObject jsonObject = JSON.parseObject(body);
        if (!Objects.equals("0", jsonObject.getString("code"))) {
            throw new BizException(jsonObject.getString("msg"));
        }
        return jsonObject;

    }

    public String getTornaApiUrl() {
        String value = configFacade.getString(ConfigKey.Open.TORNA_SERVER_ADDR, null);
        if (ObjectUtils.isEmpty(value)) {
            throw new BizException("Torna服务器地址未配置");
        }
        return StringUtils.trimTrailingCharacter(value, '/');
    }

}
