package top.mddata.workbench.service.social.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.exception.BizException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.entity.User;
import top.mddata.common.entity.UserSocialAuth;
import top.mddata.workbench.dto.SocialCallbackDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.SocialActionEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.AccountAuthenticator;
import top.mddata.workbench.service.LoginSessionService;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.service.UserSocialAuthService;
import top.mddata.workbench.service.social.SocialLoginService;
import top.mddata.workbench.service.social.SocialProviderFactory;
import top.mddata.workbench.service.social.SocialTicketService;
import top.mddata.workbench.service.social.model.SocialIdentity;
import top.mddata.workbench.service.social.model.SocialState;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.SocialLoginResultVo;
import top.mddata.workbench.vo.SocialProviderVo;

import java.util.List;

/**
 * 第三方登录编排服务实现。
 *
 * @author tangyh
 */
@Service
@RequiredArgsConstructor
public class SocialLoginServiceImpl implements SocialLoginService {

    /** 网站应用登录固定为 PC 设备类型 */
    private static final String DEVICE_TYPE_PC = "PC";

    private final SocialTicketService ticketService;
    private final SocialProviderFactory providerFactory;
    private final UserSocialAuthService userSocialAuthService;
    private final SsoUserService ssoUserService;
    private final AccountAuthenticator accountAuthenticator;
    private final LoginSessionService loginSessionService;

    @Override
    public List<SocialProviderVo> providers() {
        return providerFactory.enabledProviders().stream()
                .map(SocialProviderVo::from)
                .toList();
    }

    @Override
    public String loginUrl(SocialProviderEnum provider) {
        ArgumentAssert.notNull(provider, () -> new BizException("第三方登录平台不能为空"));
        String state = ticketService.createState(SocialState.builder()
                .provider(provider)
                .action(SocialActionEnum.LOGIN)
                .build());
        return providerFactory.get(provider).authorize(state);
    }

    @Override
    public SocialLoginResultVo login(SocialCallbackDto callback) {
        ArgumentAssert.notNull(callback, () -> new BizException("第三方登录回调参数不能为空"));
        ticketService.consumeState(callback.getState(), callback.getProvider(),
                SocialActionEnum.LOGIN);
        SocialIdentity identity = providerFactory.get(callback.getProvider())
                .exchange(callback.getCode(), callback.getState());
        return userSocialAuthService.findByIdentity(identity)
                .map(binding -> loginBoundUser(binding, identity, callback))
                .orElseGet(() -> pendingUser(identity));
    }

    private SocialLoginResultVo loginBoundUser(UserSocialAuth binding, SocialIdentity identity,
                                               SocialCallbackDto callback) {
        User user = ssoUserService.getById(binding.getUserId());
        AuthTypeEnum authType = authType(identity.getProvider());
        accountAuthenticator.checkUserState(user, authType, callback.getDeviceInfo());
        LoginVo loginVo = loginSessionService.loginAuthenticated(user, authType,
                user.getUsername(), callback.getDeviceId(), callback.getDeviceInfo(),
                DEVICE_TYPE_PC);
        return SocialLoginResultVo.success(loginVo);
    }

    private SocialLoginResultVo pendingUser(SocialIdentity identity) {
        String ticket = ticketService.createPending(identity);
        return SocialLoginResultVo.pending(ticket, identity.getProvider().getCode(),
                identity.getNickname());
    }

    private AuthTypeEnum authType(SocialProviderEnum provider) {
        ArgumentAssert.notNull(provider, () -> new BizException("第三方登录平台不能为空"));
        return provider.getAuthType();
    }
}
