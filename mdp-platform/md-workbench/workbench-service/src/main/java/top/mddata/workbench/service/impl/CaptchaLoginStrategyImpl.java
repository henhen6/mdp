package top.mddata.workbench.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.CaptchaException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.utils.SpringUtils;
import top.mddata.base.utils.StrHelper;
import top.mddata.common.cache.workbench.CaptchaCacheKeyBuilder;
import top.mddata.common.constant.ConfigKey;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.system.facade.ConfigFacade;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.event.LoginEvent;
import top.mddata.workbench.service.SsoUserService;

/**
 *
 * @author henhen6
 * @since 2025/7/10 17:25
 */
@Component(CaptchaLoginStrategyImpl.GRANT_TYPE)
@Slf4j
public class CaptchaLoginStrategyImpl extends UsernameLoginStrategyImpl {
    public static final String GRANT_TYPE = "CAPTCHA";
    private final CacheOps cacheOps;

    public CaptchaLoginStrategyImpl(CacheOps cacheOps, SystemProperties systemProperties, SsoUserService ssoUserService, ConfigFacade configFacade) {
        super(systemProperties, ssoUserService, configFacade);
        this.cacheOps = cacheOps;
    }

    @Override
    public void checkParam(LoginDto login) {
        super.checkParam(login);
        if (configFacade.getBoolean(ConfigKey.Console.LOGIN_CAPTCHA_ENABLED, true)) {
            if (StrHelper.isAnyBlank(login.getKey(), login.getCode())) {
                throw CaptchaException.wrap("请输入验证码");
            }
            CacheKey cacheKey = CaptchaCacheKeyBuilder.build(login.getKey(), GRANT_TYPE);
            CacheResult<String> code = cacheOps.get(cacheKey);
            if (StrUtil.isEmpty(code.getValue())) {
                String msg = "验证码已过期";
                SpringUtils.publishEvent(new LoginEvent(LoginLogDto.failByCheck(login.getAuthType(), login.getDeviceInfo(), login.getUsername(), msg)));
                throw CaptchaException.wrap(msg);
            }
            if (!StrUtil.equalsIgnoreCase(code.getValue(), login.getCode())) {
                String msg = "验证码不正确";
                SpringUtils.publishEvent(new LoginEvent(LoginLogDto.failByCheck(login.getAuthType(), login.getDeviceInfo(), login.getUsername(), msg)));
                throw CaptchaException.wrap(msg);
            }
            cacheOps.del(cacheKey);
        }
    }

}
