package top.mddata.workbench.handler.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.captcha.graphic.properties.GraphicCaptchaProperties;
import top.mddata.base.exception.CaptchaException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.utils.StrHelper;
import top.mddata.common.cache.workbench.CaptchaCacheKeyBuilder;
import top.mddata.common.constant.DefValConstants;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.facade.system.ConfigFacade;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.event.LoginEvent;
import top.mddata.workbench.service.user.SsoUserService;

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
    private final GraphicCaptchaProperties graphicCaptchaProperties;

    public CaptchaLoginStrategyImpl(CacheOps cacheOps, SystemProperties systemProperties, SsoUserService ssoUserService, ConfigFacade configFacade, GraphicCaptchaProperties graphicCaptchaProperties) {
        super(systemProperties, ssoUserService, configFacade);
        this.cacheOps = cacheOps;
        this.graphicCaptchaProperties = graphicCaptchaProperties;
    }

    @Override
    public void checkParam(LoginDto login) {
        super.checkParam(login);
        if (graphicCaptchaProperties.getEnabled()) {
            if (StrHelper.isAnyBlank(login.getKey(), login.getCode())) {
                throw CaptchaException.wrap("请输入验证码");
            }
            CacheKey cacheKey = CaptchaCacheKeyBuilder.build(login.getKey(), GRANT_TYPE);
            CacheResult<String> code = cacheOps.get(cacheKey);
            if (StrUtil.isEmpty(code.getValue())) {
                String msg = "验证码已过期";
                LoginLogDto dto = LoginLogDto.failByCheck(login.getAuthType(), login.getDeviceInfo(), login.getUsername(), msg)
                        .setAppKey(DefValConstants.WORKBENCH_APP_KEY).setAppName(DefValConstants.WORKBENCH_APP_NAME);
                SpringUtil.publishEvent(new LoginEvent(dto));
                throw CaptchaException.wrap(msg);
            }
            if (!StrUtil.equalsIgnoreCase(code.getValue(), login.getCode())) {
                String msg = "验证码不正确";
                LoginLogDto dto = LoginLogDto.failByCheck(login.getAuthType(), login.getDeviceInfo(), login.getUsername(), msg)
                        .setAppKey(DefValConstants.WORKBENCH_APP_KEY).setAppName(DefValConstants.WORKBENCH_APP_NAME);
                SpringUtil.publishEvent(new LoginEvent(dto));
                throw CaptchaException.wrap(msg);
            }
            cacheOps.del(cacheKey);
        }
    }

}
