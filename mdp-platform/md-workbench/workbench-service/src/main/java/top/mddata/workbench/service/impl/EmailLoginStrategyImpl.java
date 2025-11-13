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
import top.mddata.workbench.enumeration.LoginStatusEnum;
import top.mddata.workbench.enumeration.MsgTemplateCodeEnum;
import top.mddata.workbench.event.LoginEvent;
import top.mddata.workbench.event.model.LoginStatusDto;
import top.mddata.workbench.service.SsoUserService;

/**
 * 邮箱 + 邮箱验证码
 * @author henhen6
 * @since 2025/7/10 17:25
 */
@Component(EmailLoginStrategyImpl.GRANT_TYPE)
@Slf4j
public class EmailLoginStrategyImpl extends UsernameLoginStrategyImpl {
    public static final String GRANT_TYPE = "EMAIL";
    private final CacheOps cacheOps;

    public EmailLoginStrategyImpl(CacheOps cacheOps, SystemProperties systemProperties, SsoUserService ssoUserService, ConfigFacade configFacade) {
        super(systemProperties, ssoUserService, configFacade);
        this.cacheOps = cacheOps;
    }

    @Override
    public void checkParam(LoginDto login) {
        String email = login.getUsername();
        if (StrHelper.isAnyBlank(email)) {
            throw new BizException("请输入邮箱");
        }

        if (systemProperties.getVerifyCaptcha()) {
            if (StrHelper.isAnyBlank(login.getKey(), login.getCode())) {
                throw new BizException("请输入验证码");
            }
            CacheKey cacheKey = CaptchaCacheKeyBuilder.build(login.getKey(), MsgTemplateCodeEnum.EMAIL_LOGIN.name());
            CacheResult<String> code = cacheOps.get(cacheKey);
            if (StrUtil.isEmpty(code.getValue())) {
                String msg = "验证码已过期";
                SpringUtils.publishEvent(new LoginEvent(LoginStatusDto.fail(login.getUsername(), LoginStatusEnum.EMAIL_CODE_ERROR, msg)));
                throw new BizException(msg);
            }
            if (!StrUtil.equalsIgnoreCase(code.getValue(), login.getCode())) {
                String msg = "验证码不正确";
                SpringUtils.publishEvent(new LoginEvent(LoginStatusDto.fail(login.getUsername(), LoginStatusEnum.EMAIL_CODE_ERROR, msg)));
                throw new BizException(msg);
            }
            cacheOps.del(cacheKey);

        }
    }

    @Override
    public void checkUserPassword(LoginDto login, User user) {
        // 判断用户状态
        if (user == null) {
            throw new BizException("邮箱或验证码错误!");
        }
    }

    @Override
    public User getUser(String value) {
        return ssoUserService.getByEmail(value);
    }
}
