package top.mddata.console.system.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.console.system.facade.ConfigFacade;
import top.mddata.console.system.service.ConfigService;
import top.mddata.console.system.vo.ConfigVo;

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
}
