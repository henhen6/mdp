package top.mddata.workbench.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.utils.SpringUtils;
import top.mddata.base.utils.StrHelper;
import top.mddata.common.cache.workbench.CaptchaCacheKeyBuilder;
import top.mddata.common.entity.User;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.system.facade.ConfigFacade;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.enumeration.MsgTemplateCodeEnum;
import top.mddata.workbench.event.LoginEvent;
import top.mddata.workbench.service.SsoUserService;

/**
 * 手机 + 短信验证码
 * @author henhen6
 * @since 2025/7/10 17:25
 */
@Component(PhoneLoginStrategyImpl.GRANT_TYPE)
@Slf4j
public class PhoneLoginStrategyImpl extends UsernameLoginStrategyImpl {
    public static final String GRANT_TYPE = "PHONE";
    private final CacheOps cacheOps;

    public PhoneLoginStrategyImpl(CacheOps cacheOps, SystemProperties systemProperties, SsoUserService ssoUserService, ConfigFacade configFacade) {
        super(systemProperties, ssoUserService, configFacade);
        this.cacheOps = cacheOps;
    }

    @Override
    public void checkParam(LoginDto login) {
        String phone = login.getUsername();
        if (StrHelper.isAnyBlank(phone)) {
            throw new BizException("请输入手机号证码");
        }

        if (systemProperties.getVerifyCaptcha()) {
            if (StrHelper.isAnyBlank(login.getKey(), login.getCode())) {
                throw new BizException("请输入验证码");
            }
            CacheKey cacheKey = CaptchaCacheKeyBuilder.build(login.getKey(), MsgTemplateCodeEnum.PHONE_LOGIN.name());
            CacheResult<String> code = cacheOps.get(cacheKey);
            if (StrUtil.isEmpty(code.getValue())) {
                String msg = "验证码已过期";
                SpringUtils.publishEvent(new LoginEvent(LoginLogDto.failByCheck(login.getAuthType(), login.getDeviceInfo(), login.getUsername(), msg)));
                throw new BizException(msg);
            }
            if (!StrUtil.equalsIgnoreCase(code.getValue(), login.getCode())) {
                String msg = "验证码不正确";
                SpringUtils.publishEvent(new LoginEvent(LoginLogDto.failByCheck(login.getAuthType(), login.getDeviceInfo(), login.getUsername(), msg)));
                throw new BizException(msg);
            }
            cacheOps.del(cacheKey);
        }
    }


    @Override
    public void checkUserPassword(LoginDto login, User user) {
        // 判断用户状态
        if (user == null) {
            throw new BizException("手机或验证码错误!");
        }
    }

    @Override
    public User getUser(String value) {
        return ssoUserService.getByPhone(value);
    }
}
