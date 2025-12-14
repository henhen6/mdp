package top.mddata.workbench.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.base.exception.BizException;
import top.mddata.base.utils.DateUtils;
import top.mddata.base.utils.SpringUtils;
import top.mddata.base.utils.StrHelper;
import top.mddata.base.utils.UaSecureUtil;
import top.mddata.common.constant.ConfigKey;
import top.mddata.common.entity.User;
import top.mddata.common.enumeration.organization.UserSourceEnum;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.system.facade.ConfigFacade;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.event.LoginEvent;
import top.mddata.workbench.service.LoginStrategy;
import top.mddata.workbench.service.SsoUserService;

import java.time.LocalDateTime;

/**
 * 用户名 + 密码登录
 *
 * @author henhen6
 * @since 2025/7/10 09:25
 */
@Component(UsernameLoginStrategyImpl.GRANT_TYPE)
@RequiredArgsConstructor
@Slf4j
public class UsernameLoginStrategyImpl implements LoginStrategy {
    public static final String GRANT_TYPE = "USERNAME";
    protected final SystemProperties systemProperties;
    protected final SsoUserService ssoUserService;
    private final ConfigFacade configFacade;

    @Override
    public void checkParam(LoginDto login) {
        String username = login.getUsername();
        String password = login.getPassword();
        if (StrHelper.isAnyBlank(username, password)) {
            throw new BizException("请输入用户名或密码");
        }
    }

    @Override
    public void checkUserPassword(LoginDto login, User user) {
        // 判断用户状态
        if (user == null) {
            throw new BizException("用户名或密码错误!");
        }

        // 方便开发、测试、演示环境 开发者登录别人的账号，生产环境禁用。
        if (!systemProperties.getVerifyPassword()) {
            return;
        }

        // 是否启用 密码过期禁止登录
        Boolean passwordExpireNotAllowLogin = configFacade.getBoolean(ConfigKey.Workbench.PASSWORD_EXPIRE_NOT_ALLOW_LOGIN, false);

        // 密码过期
        if (passwordExpireNotAllowLogin && user.getPwExpireTime() != null && LocalDateTime.now().isAfter(user.getPwExpireTime())) {
            String msg = "用户密码已过期，请修改密码或者联系管理员重置!";
            SpringUtils.publishEvent(new LoginEvent(LoginLogDto.failByCheck(login.getAuthType(), login.getDeviceInfo(), user.getUsername(), msg)));
            throw new BizException(msg);
        }

        // 用户锁定
        Integer passwordErrorNum = Convert.toInt(user.getPwErrorNum(), 0);

        Integer maxPasswordErrorNum = configFacade.getInteger(ConfigKey.Workbench.PASSWORD_ERROR_LOCK_ACCOUNT, 0);
        if (maxPasswordErrorNum > 0 && passwordErrorNum >= maxPasswordErrorNum) {
            log.info("[{}][{}], 输错密码次数：{}, 最大限制次数:{}", user.getName(), user.getId(), passwordErrorNum, maxPasswordErrorNum);

            /*
             * (最后一次输错密码的时间 + 锁定时间) 与 (当前时间) 比较
             * (最后一次输错密码的时间 + 锁定时间) > (当前时间) 表示未解锁
             * (最后一次输错密码的时间 + 锁定时间) < (当前时间) 表示自动解锁，并重置错误次数和最后一次错误时间
             */
            String time = configFacade.getString(ConfigKey.Workbench.PASSWORD_ERROR_LOCK_USER_TIME, "10m");
            LocalDateTime passwordErrorLockExpireTime = DateUtils.conversionDateTime(user.getPwErrorLastTime(), time);
            log.info("密码最后一次输错后，解锁时间: {}", passwordErrorLockExpireTime);
            // passwordErrorLockTime(锁定到期时间) > 当前时间
            if (passwordErrorLockExpireTime.isAfter(LocalDateTime.now())) {
                // 登录失败事件
                String msg = StrUtil.format("密码连续输错次数已超过最大限制：{}次,用户将被锁定至: {}", maxPasswordErrorNum, DateUtils.format(passwordErrorLockExpireTime));
                SpringUtils.publishEvent(new LoginEvent(LoginLogDto.failByCheck(login.getAuthType(), login.getDeviceInfo(), user.getUsername(), msg)));
                throw new BizException(msg);
            }
        }

        String passwordMd5 = null;
        if (StrUtil.equals(UserSourceEnum.PLATFORM.getCode(), user.getUserSource())) {
            passwordMd5 = SecureUtil.sha256(login.getPassword() + user.getSalt());
        } else {
            passwordMd5 = SecureUtil.sha256(UaSecureUtil.md5BySalt(user.getPassword(), user.getSalt()));
        }
        if (!passwordMd5.equalsIgnoreCase(user.getPassword())) {
            String msg = StrUtil.format("用户名或密码错误{}次，连续输错{}次您将被锁定！", (Convert.toInt(user.getPwErrorNum(), 0) + 1), maxPasswordErrorNum);
            // 密码错误事件
            LoginLogDto loginLogDto = LoginLogDto.failByCheck(login.getAuthType(), login.getDeviceInfo(), user.getUsername(), msg);
            loginLogDto.setPasswordError(true);
            SpringUtils.publishEvent(new LoginEvent(loginLogDto));
            throw new BizException(msg);
        }

    }

    @Override
    public User getUser(String value) {
        return ssoUserService.getByUsername(value);
    }

    @Override
    public void checkUserState(LoginDto login, User user) {
        // 用户被禁用
        if (user.getState() == null || !user.getState()) {
            String msg = "您已被禁用，请联系管理员开通账号！";
            SpringUtils.publishEvent(new LoginEvent(LoginLogDto.failByCheck(login.getAuthType(), login.getDeviceInfo(), user.getUsername(), msg)));
            throw new BizException(msg);
        }
    }
}
