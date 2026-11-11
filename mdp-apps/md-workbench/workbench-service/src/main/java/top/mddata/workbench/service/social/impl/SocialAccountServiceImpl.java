package top.mddata.workbench.service.social.impl;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.exception.BizException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.entity.User;
import top.mddata.common.entity.UserSocialAuth;
import top.mddata.workbench.dto.SocialCallbackDto;
import top.mddata.workbench.dto.SocialReauthDto;
import top.mddata.workbench.dto.SocialReauthUrlDto;
import top.mddata.workbench.dto.SocialTicketDto;
import top.mddata.workbench.dto.SocialTransferDto;
import top.mddata.workbench.dto.SocialUnbindDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.SocialActionEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.enumeration.SocialReauthMethodEnum;
import top.mddata.workbench.service.AccountAuthenticator;
import top.mddata.workbench.service.LoginSessionService;
import top.mddata.workbench.service.UserSocialAuthService;
import top.mddata.workbench.service.social.SocialAccountService;
import top.mddata.workbench.service.social.SocialProvider;
import top.mddata.workbench.service.social.SocialProviderFactory;
import top.mddata.workbench.service.social.SocialTicketService;
import top.mddata.workbench.service.social.model.SocialIdentity;
import top.mddata.workbench.service.social.model.SocialReauth;
import top.mddata.workbench.service.social.model.SocialState;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.SocialBindingVo;
import top.mddata.workbench.vo.SocialReauthTicketVo;

import java.util.List;
import java.util.Objects;

/**
 * 个人中心第三方账号管理服务实现。
 *
 * @author tangyh
 */
@Service
@RequiredArgsConstructor
public class SocialAccountServiceImpl implements SocialAccountService {

    /** 网站应用操作固定为 PC 设备类型 */
    private static final String DEVICE_TYPE_PC = "PC";

    private final SocialTicketService ticketService;
    private final UserSocialAuthService userSocialAuthService;
    private final AccountAuthenticator accountAuthenticator;
    private final SocialProviderFactory providerFactory;
    private final LoginSessionService loginSessionService;

    @Override
    public List<SocialBindingVo> list(Long userId) {
        ArgumentAssert.notNull(userId, () -> new BizException("用户未登录"));
        List<SocialProviderEnum> enabled = providerFactory.enabledProviders();
        List<UserSocialAuth> bindings = userSocialAuthService.listByUser(userId);
        return enabled.stream()
                .map(provider -> bindings.stream()
                        .filter(binding -> provider.name().equals(binding.getProvider()))
                        .findFirst()
                        .map(binding -> new SocialBindingVo(provider.getCode(), true,
                                binding.getNickname(), binding.getBoundAt()))
                        .orElseGet(() -> new SocialBindingVo(provider.getCode(), false,
                                null, null)))
                .toList();
    }

    @Override
    public SocialReauthTicketVo reauth(Long userId, SocialReauthDto dto) {
        ArgumentAssert.notNull(userId, () -> new BizException("用户未登录"));
        ArgumentAssert.notNull(dto, () -> new BizException("二次验证参数不能为空"));
        User verified = accountAuthenticator.authenticate(dto.getLogin());
        ArgumentAssert.isTrue(userId.equals(verified.getId()),
                () -> new BizException("验证账号与当前登录账号不一致"));

        SocialReauth reauth = SocialReauth.builder()
                .userId(userId)
                .action(dto.getAction())
                .provider(dto.getProvider())
                .method(reauthMethod(dto.getLogin().getAuthType()))
                .build();
        return new SocialReauthTicketVo(ticketService.createReauth(reauth));
    }

    @Override
    public String reauthUrl(Long userId, SocialProviderEnum reauthProvider,
                            SocialReauthUrlDto dto) {
        ArgumentAssert.notNull(userId, () -> new BizException("用户未登录"));
        ArgumentAssert.notNull(dto, () -> new BizException("重新授权参数不能为空"));
        ArgumentAssert.isTrue(userSocialAuthService.findByUserAndProvider(userId, reauthProvider)
                        .isPresent(),
                () -> new BizException("当前账号未绑定[" + reauthProvider.getLabel() + "]平台"));

        SocialState state = SocialState.builder()
                .provider(reauthProvider)
                .action(SocialActionEnum.REAUTH)
                .userId(userId)
                .requestedAction(dto.getAction())
                .requestedProvider(dto.getProvider())
                .build();
        String ticket = ticketService.createState(state);
        SocialProvider provider = providerFactory.get(reauthProvider);
        return provider.authorize(ticket);
    }

    @Override
    public SocialReauthTicketVo reauthByCallback(Long userId, SocialCallbackDto callback) {
        ArgumentAssert.notNull(userId, () -> new BizException("用户未登录"));
        ArgumentAssert.notNull(callback, () -> new BizException("授权回调不能为空"));
        SocialState state = ticketService.consumeState(callback.getState(),
                callback.getProvider(), SocialActionEnum.REAUTH);
        ArgumentAssert.isTrue(userId.equals(state.getUserId()),
                () -> new BizException("授权用户与当前登录用户不一致"));

        SocialProvider provider = providerFactory.get(callback.getProvider());
        SocialIdentity identity = provider.exchange(callback.getCode(), callback.getState());
        UserSocialAuth binding = userSocialAuthService
                .findByUserAndProvider(userId, callback.getProvider())
                .orElseThrow(() -> new BizException("当前账号未绑定该平台"));
        ArgumentAssert.isTrue(Objects.equals(binding.getClientId(), identity.getClientId())
                        && Objects.equals(binding.getOpenId(), identity.getOpenId()),
                () -> new BizException("扫码身份与当前绑定账号不一致"));

        SocialReauth reauth = SocialReauth.builder()
                .userId(userId)
                .action(state.getRequestedAction())
                .provider(state.getRequestedProvider())
                .method(SocialReauthMethodEnum.SOCIAL)
                .build();
        return new SocialReauthTicketVo(ticketService.createReauth(reauth));
    }

    @Override
    public String bindUrl(Long userId, SocialProviderEnum provider, SocialTicketDto dto) {
        ArgumentAssert.notNull(userId, () -> new BizException("用户未登录"));
        ArgumentAssert.notNull(dto, () -> new BizException("绑定参数不能为空"));
        ticketService.getReauth(dto.getReauthTicket(), userId, SocialActionEnum.BIND, provider);

        SocialState state = SocialState.builder()
                .provider(provider)
                .action(SocialActionEnum.BIND)
                .userId(userId)
                .reauthTicketId(dto.getReauthTicket())
                .build();
        String ticket = ticketService.createState(state);
        return providerFactory.get(provider).authorize(ticket);
    }

    @Override
    public void bind(Long userId, SocialCallbackDto callback) {
        ArgumentAssert.notNull(userId, () -> new BizException("用户未登录"));
        ArgumentAssert.notNull(callback, () -> new BizException("授权回调不能为空"));
        SocialState state = ticketService.consumeState(callback.getState(),
                callback.getProvider(), SocialActionEnum.BIND);
        ArgumentAssert.isTrue(userId.equals(state.getUserId()),
                () -> new BizException("授权用户与当前登录用户不一致"));
        ArgumentAssert.isTrue(state.getReauthTicketId() != null
                        && !state.getReauthTicketId().isBlank(),
                () -> new BizException("绑定操作缺少二次验证票据"));
        ticketService.getReauth(state.getReauthTicketId(), userId,
                SocialActionEnum.BIND, callback.getProvider());
        SocialIdentity identity = providerFactory.get(callback.getProvider())
                .exchange(callback.getCode(), callback.getState());
        ticketService.consumeReauth(state.getReauthTicketId());
        userSocialAuthService.bind(userId, identity);
    }

    @Override
    public void unbind(Long userId, SocialUnbindDto dto) {
        ArgumentAssert.notNull(userId, () -> new BizException("用户未登录"));
        ArgumentAssert.notNull(dto, () -> new BizException("解绑参数不能为空"));
        SocialReauth reauth = ticketService.getReauth(dto.getReauthTicket(), userId,
                SocialActionEnum.UNBIND, dto.getProvider());
        UserSocialAuth binding = userSocialAuthService
                .findByUserAndProvider(userId, dto.getProvider())
                .orElseThrow(() -> new BizException("当前账号未绑定该平台"));
        if (reauth.getMethod() == SocialReauthMethodEnum.SOCIAL) {
            ArgumentAssert.isTrue(userSocialAuthService.countByUser(userId) > 1,
                    () -> new BizException("解绑后将无法登录，请先补充联系方式或绑定其他平台"));
        }
        ticketService.consumeReauth(dto.getReauthTicket());
        userSocialAuthService.unbind(binding.getId(), userId);
    }

    @Override
    public LoginVo transfer(Long currentUserId, SocialTransferDto dto) {
        ArgumentAssert.notNull(currentUserId, () -> new BizException("用户未登录"));
        ArgumentAssert.notNull(dto, () -> new BizException("转移参数不能为空"));
        ticketService.getReauth(dto.getReauthTicket(), currentUserId,
                SocialActionEnum.TRANSFER, dto.getProvider());
        UserSocialAuth binding = userSocialAuthService
                .findByUserAndProvider(currentUserId, dto.getProvider())
                .orElseThrow(() -> new BizException("当前账号未绑定该平台"));
        User target = accountAuthenticator.authenticate(dto.getTargetLogin());
        ArgumentAssert.isFalse(currentUserId.equals(target.getId()),
                () -> new BizException("目标账号不能与当前账号相同"));
        ArgumentAssert.isTrue(userSocialAuthService.findByUserAndProvider(
                        target.getId(), dto.getProvider()).isEmpty(),
                () -> new BizException("目标账号已绑定该平台"));

        ticketService.consumeReauth(dto.getReauthTicket());
        userSocialAuthService.transfer(binding.getId(), currentUserId, target.getId(),
                currentUserId);
        StpUtil.logout();
        return loginSessionService.loginAuthenticated(target, authType(dto.getProvider()),
                target.getUsername(), dto.getTargetLogin().getDeviceId(),
                dto.getTargetLogin().getDeviceInfo(), DEVICE_TYPE_PC);
    }

    private AuthTypeEnum authType(SocialProviderEnum provider) {
        ArgumentAssert.notNull(provider, () -> new BizException("第三方平台不能为空"));
        return switch (provider) {
            case WECHAT_OPEN -> AuthTypeEnum.WECHAT_OPEN;
            case QQ -> AuthTypeEnum.QQ;
            case DINGTALK -> AuthTypeEnum.DINGTALK;
        };
    }

    private SocialReauthMethodEnum reauthMethod(AuthTypeEnum authType) {
        ArgumentAssert.notNull(authType, () -> new BizException("账号验证方式不能为空"));
        return switch (authType) {
            case CAPTCHA, USERNAME -> SocialReauthMethodEnum.PASSWORD;
            case PHONE -> SocialReauthMethodEnum.PHONE;
            case EMAIL -> SocialReauthMethodEnum.EMAIL;
            default -> throw new BizException("不支持的账号验证方式");
        };
    }
}