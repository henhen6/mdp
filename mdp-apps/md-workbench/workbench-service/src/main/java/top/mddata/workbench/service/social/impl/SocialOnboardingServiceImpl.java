package top.mddata.workbench.service.social.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.exception.BizException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.dto.UserSocialRegisterDto;
import top.mddata.common.entity.User;
import top.mddata.common.entity.UserSocialAuth;
import top.mddata.console.facade.organization.UserFacade;
import top.mddata.workbench.dto.SocialBindExistingDto;
import top.mddata.workbench.dto.SocialRegisterDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.AccountAuthenticator;
import top.mddata.workbench.service.LoginSessionService;
import top.mddata.workbench.service.UserSocialAuthService;
import top.mddata.workbench.service.social.SocialOnboardingService;
import top.mddata.workbench.service.social.SocialTicketService;
import top.mddata.workbench.service.social.model.PendingSocialUser;
import top.mddata.workbench.service.social.model.SocialIdentity;
import top.mddata.workbench.vo.LoginVo;

import java.util.Base64;
import java.security.SecureRandom;

/**
 * 第三方登录首次选择服务实现。
 *
 * @author tangyh
 */
@Service
@RequiredArgsConstructor
public class SocialOnboardingServiceImpl implements SocialOnboardingService {

    /** 网站应用登录固定为 PC 设备类型 */
    private static final String DEVICE_TYPE_PC = "PC";

    /** 初始密码随机字节数 */
    private static final int PASSWORD_BYTES = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SocialTicketService ticketService;
    private final UserSocialAuthService userSocialAuthService;
    private final AccountAuthenticator accountAuthenticator;
    private final LoginSessionService loginSessionService;
    private final UserFacade userFacade;

    @Override
    public LoginVo bindExisting(SocialBindExistingDto dto) {
        ArgumentAssert.notNull(dto, () -> new BizException("绑定参数不能为空"));
        // 先验票：票据失效时不得消耗手机/邮箱验证码或密码尝试
        PendingSocialUser pending = ticketService.getPending(dto.getPendingTicket());
        SocialIdentity identity = pending.getIdentity();
        User target;
        try {
            target = accountAuthenticator.authenticate(dto.getLogin());
        } catch (BizException ex) {
            // 凭据失败只累计次数，不消费票据，达到上限后由票据服务使其失效
            ticketService.registerPendingFailure(dto.getPendingTicket());
            throw ex;
        }
        ArgumentAssert.isTrue(userSocialAuthService
                        .findByUserAndProvider(target.getId(), identity.getProvider()).isEmpty(),
                () -> new BizException("目标账号已绑定该平台，请先解绑"));
        ArgumentAssert.isTrue(userSocialAuthService.findByIdentity(identity).isEmpty(),
                () -> new BizException("该第三方账号已绑定其他用户"));
        ticketService.consumePending(dto.getPendingTicket());
        userSocialAuthService.bind(target.getId(), identity);
        return loginSessionService.loginAuthenticated(target, authType(identity.getProvider()),
                target.getUsername(), dto.getLogin().getDeviceId(),
                dto.getLogin().getDeviceInfo(), DEVICE_TYPE_PC);
    }

    @Override
    public LoginVo register(SocialRegisterDto dto) {
        ArgumentAssert.notNull(dto, () -> new BizException("注册参数不能为空"));
        ArgumentAssert.isTrue(Boolean.TRUE.equals(dto.getAgreement()),
                () -> new BizException("请先阅读并同意用户协议"));
        PendingSocialUser pending = ticketService.getPending(dto.getPendingTicket());
        SocialIdentity identity = pending.getIdentity();
        ticketService.consumePending(dto.getPendingTicket());

        User user = new User();
        user.setUsername("social_" + UUID.randomUUID().toString(true));
        user.setPassword(randomPassword());
        user.setName(StrUtil.blankToDefault(dto.getName(), identity.getNickname()));
        user.setNature(dto.getNature());

        UserSocialAuth binding = new UserSocialAuth();
        binding.setProvider(identity.getProvider().name());
        binding.setClientId(identity.getClientId());
        binding.setOpenId(identity.getOpenId());
        binding.setUnionId(identity.getUnionId());
        binding.setNickname(identity.getNickname());

        Long userId = userFacade.registerBySocial(new UserSocialRegisterDto(user, binding));
        ArgumentAssert.notNull(userId, () -> new BizException("社交账号注册未返回用户ID"));
        user.setId(userId);
        return loginSessionService.loginAuthenticated(user, authType(identity.getProvider()),
                user.getUsername(), dto.getDeviceId(), dto.getDeviceInfo(), DEVICE_TYPE_PC);
    }

    private String randomPassword() {
        byte[] bytes = new byte[PASSWORD_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private AuthTypeEnum authType(SocialProviderEnum provider) {
        ArgumentAssert.notNull(provider, () -> new BizException("第三方登录平台不能为空"));
        return provider.getAuthType();
    }
}
