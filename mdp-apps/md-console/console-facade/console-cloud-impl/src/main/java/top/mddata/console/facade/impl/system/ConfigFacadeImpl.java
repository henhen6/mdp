package top.mddata.console.facade.impl.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.base.exception.BizException;
import top.mddata.console.facade.api.system.ConfigApi;
import top.mddata.console.facade.system.ConfigFacade;
import top.mddata.console.vo.system.ConfigVo;

/**
 *
 * @author henhen6
 * @since 2025/10/10 23:55
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigFacadeImpl implements ConfigFacade {
    private final ConfigApi configApi;

    @Override
    public ConfigVo getConfig(String uniqKey) {
        R<ConfigVo> result = configApi.getConfig(uniqKey);
        return result.getIsSuccess() ? result.getData() : null;
    }

    @Override
    public Long getLong(String uniqKey, Long defaultValue) {
        R<Long> result = configApi.getLong(uniqKey, defaultValue);
        return result.getIsSuccess() ? result.getData() : null;
    }

    @Override
    public Integer getInteger(String uniqKey, Integer defaultValue) {
        R<Integer> result = configApi.getInteger(uniqKey, defaultValue);
        return result.getIsSuccess() ? result.getData() : null;
    }

    @Override
    public String getString(String uniqKey, String defaultValue) {
        R<String> result = configApi.getString(uniqKey, defaultValue);
        return result.getIsSuccess() ? result.getData() : null;
    }

    @Override
    public Boolean getBoolean(String uniqKey, Boolean defaultValue) {
        R<Boolean> result = configApi.getBoolean(uniqKey, defaultValue);
        return result.getIsSuccess() ? result.getData() : false;
    }

    @Override
    public Boolean getBooleanRequired(String uniqKey, Boolean defaultValue) {
        R<Boolean> result = configApi.getBoolean(uniqKey, defaultValue);
        if (result == null || !Boolean.TRUE.equals(result.getIsSuccess())) {
            // 安全敏感配置读取失败必须 fail-closed，禁止回退默认值放行
            Integer respCode = result == null ? null : result.getCode();
            String respMsg = result == null ? null : result.getMsg();
            log.error("[安全配置] 读取布尔配置失败，拒绝放行，uniqKey={}, respCode={}, respMsg={}",
                    uniqKey, respCode, respMsg);
            throw new BizException("读取系统安全策略失败，请稍后重试");
        }
        // 契约：defaultValue 仅在服务端正常响应且值为空时使用
        return result.getData() != null ? result.getData() : defaultValue;
    }
}
