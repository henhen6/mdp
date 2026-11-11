package top.mddata.workbench.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mddata.base.exception.BizException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.entity.User;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.handler.LoginStrategy;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 账号认证组件：按认证类型复用 {@link LoginStrategy} 完成账号类登录认证。
 *
 * <p>社交登录类型（微信扫码、QQ、钉钉等）只用于登录日志，禁止在此走账号密码策略；
 * 社交登录在认证完成后应通过 {@link LoginSessionService#loginAuthenticated} 签发会话。</p>
 *
 * @author tangyh
 */
@Component
@RequiredArgsConstructor
public class AccountAuthenticator {

    private static final Set<AuthTypeEnum> SUPPORTED = EnumSet.of(
            AuthTypeEnum.CAPTCHA, AuthTypeEnum.USERNAME,
            AuthTypeEnum.PHONE, AuthTypeEnum.EMAIL);

    private final Map<String, LoginStrategy> loginStrategy;

    /**
     * 执行账号认证，返回已通过密码校验与状态校验的用户。
     *
     * @param login 登录参数
     * @return 已认证用户
     */
    public User authenticate(LoginDto login) {
        ArgumentAssert.notNull(login, () -> new BizException("登录参数不能为空"));
        ArgumentAssert.isTrue(SUPPORTED.contains(login.getAuthType()),
                () -> new BizException("不支持的账号验证方式"));
        LoginStrategy strategy = loginStrategy.get(login.getAuthType().name());
        ArgumentAssert.notNull(strategy, () -> new BizException("未配置对应的账号验证策略"));

        strategy.checkParam(login);
        User user = strategy.getUser(login.getUsername());
        strategy.checkUserPassword(login, user);
        strategy.checkUserState(login, user);
        return user;
    }

    /**
     * 对已通过其他方式（如社交登录）确认身份的用户复用账号状态校验。
     *
     * <p>禁用、删除等状态规则以用户名策略为唯一实现，日志类型保留社交认证类型。</p>
     *
     * @param user       已确认身份的用户
     * @param type       认证类型（用于登录日志）
     * @param deviceInfo 设备信息
     */
    public void checkUserState(User user, AuthTypeEnum type, String deviceInfo) {
        ArgumentAssert.notNull(user, () -> new BizException("账号不存在或已被删除"));
        LoginStrategy strategy = loginStrategy.get(AuthTypeEnum.USERNAME.name());
        ArgumentAssert.notNull(strategy, () -> new BizException("未配置用户名账号验证策略"));

        LoginDto context = new LoginDto();
        context.setAuthType(type);
        context.setUsername(user.getUsername());
        context.setDeviceInfo(deviceInfo);
        strategy.checkUserState(context, user);
    }
}
