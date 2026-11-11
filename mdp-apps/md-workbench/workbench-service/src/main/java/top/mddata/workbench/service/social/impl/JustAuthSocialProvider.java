package top.mddata.workbench.service.social.impl;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import top.mddata.base.exception.BizException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.workbench.config.SocialLoginProperties;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.social.SocialProvider;
import top.mddata.workbench.service.social.model.SocialIdentity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 基于 JustAuth 的第三方登录平台适配器。
 *
 * @author tangyh
 */
@Slf4j
public class JustAuthSocialProvider implements SocialProvider {

    private final SocialProviderEnum provider;
    private final SocialLoginProperties.ProviderConfig config;
    private final AuthRequest authRequest;

    public JustAuthSocialProvider(SocialProviderEnum provider,
                                  SocialLoginProperties.ProviderConfig config,
                                  AuthRequest authRequest) {
        this.provider = provider;
        this.config = config;
        this.authRequest = authRequest;
    }

    @Override
    public String authorize(String state) {
        String authorizeUrl = authRequest.authorize(state);
        String redirectUri = config.getRedirectUri();
        if (StrUtil.isBlank(redirectUri) || !redirectUri.contains("#")) {
            return authorizeUrl;
        }
        // JustAuth 默认不编码 redirect_uri，hash 会导致后续 state 等参数被当成 fragment
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return authorizeUrl.replace(redirectUri, encodedRedirectUri);
    }

    @Override
    public SocialIdentity exchange(String code, String state) {
        AuthResponse<AuthUser> response = authRequest.login(AuthCallback.builder()
                .code(code)
                .state(state)
                .build());
        if (!response.ok() || response.getData() == null) {
            log.error("第三方授权失败，provider={}，responseCode={}", provider, response.getCode());
            throw new BizException("第三方授权失败，请重新授权");
        }
        AuthUser user = response.getData();
        AuthToken token = user.getToken();
        String openId = resolveOpenId(user, token);
        String unionId = token == null ? null : token.getUnionId();
        ArgumentAssert.isTrue(StrUtil.isNotBlank(openId), "第三方未返回有效用户标识");
        return SocialIdentity.builder()
                .provider(provider)
                .clientId(config.getClientId())
                .openId(openId)
                .unionId(unionId)
                .nickname(StrUtil.blankToDefault(user.getNickname(), provider.getLabel()))
                .build();
    }

    private String resolveOpenId(AuthUser user, AuthToken token) {
        // Gitee 的用户 id 位于 uuid；QQ 的 uuid 可能是 unionId，不能作为通用回退
        if (provider == SocialProviderEnum.GITEE) {
            return user.getUuid();
        }
        return token == null ? null : token.getOpenId();
    }
}
