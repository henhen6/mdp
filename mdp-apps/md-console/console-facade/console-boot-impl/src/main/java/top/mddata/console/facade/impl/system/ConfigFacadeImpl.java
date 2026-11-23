package top.mddata.console.facade.impl.system;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.console.facade.system.ConfigFacade;
import top.mddata.console.service.system.ConfigService;
import top.mddata.console.vo.system.ConfigVo;

/**
 *
 * @author henhen6
 * @since 2025/10/10 23:55
 */
@Service
@RequiredArgsConstructor
public class ConfigFacadeImpl implements ConfigFacade {
    private final ConfigService configService;

    @Override
    public ConfigVo getConfig(String uniqKey) {
        return configService.getConfig(uniqKey);
    }

    @Override
    public Long getLong(String uniqKey, Long defaultValue) {
        return configService.getLong(uniqKey, defaultValue);
    }

    @Override
    public Integer getInteger(String uniqKey, Integer defaultValue) {
        return configService.getInteger(uniqKey, defaultValue);
    }

    @Override
    public String getString(String uniqKey, String defaultValue) {
        return configService.getString(uniqKey, defaultValue);
    }

    @Override
    public Boolean getBoolean(String uniqKey, Boolean defaultValue) {
        return configService.getBoolean(uniqKey, defaultValue);
    }

    @Override
    public Boolean getBooleanRequired(String uniqKey, Boolean defaultValue) {
        // 安全敏感读取：不吞异常，让读取失败自然向上抛出
        return configService.getBoolean(uniqKey, defaultValue);
    }
}
