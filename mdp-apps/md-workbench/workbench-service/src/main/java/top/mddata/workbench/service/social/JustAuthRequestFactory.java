package top.mddata.workbench.service.social;

import com.xkcoding.http.config.HttpConfig;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthDingTalkV2Request;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthQqRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import org.springframework.stereotype.Component;
import top.mddata.workbench.config.SocialLoginProperties;
import top.mddata.workbench.enumeration.SocialProviderEnum;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * JustAuth 请求对象工厂。
 *
 * @author tangyh
 */
@Component
public class JustAuthRequestFactory {

    /**
     * 按平台创建 JustAuth 请求
     *
     * @param provider 第三方平台
     * @param config   平台配置
     * @return JustAuth 请求对象
     */
    public AuthRequest create(SocialProviderEnum provider,
                              SocialLoginProperties.ProviderConfig config) {
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setTimeout(config.getHttpTimeoutMillis());
        String redirectUri = config.getRedirectUri();
        if (provider == SocialProviderEnum.GITEE && redirectUri != null && redirectUri.contains("#")) {
            // JustAuth 不编码查询参数，hash 会导致授权和令牌请求中的回调地址被截断
            redirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        }
        AuthConfig authConfig = AuthConfig.builder()
                .clientId(config.getClientId())
                .clientSecret(config.getClientSecret())
                .redirectUri(redirectUri)
                .unionId(config.isUnionId())
                .dingTalkCorpId(config.getCorpId())
                .httpConfig(httpConfig)
                // state 已由 SocialTicketService 在服务端独立校验，JustAuth 内部不再重复校验
                .ignoreCheckState(true)
                .build();
        return switch (provider) {
            case WECHAT_OPEN -> new AuthWeChatOpenRequest(authConfig);
            case QQ -> new AuthQqRequest(authConfig);
            case DINGTALK -> new AuthDingTalkV2Request(authConfig);
            case GITEE -> new AuthGiteeRequest(authConfig);
        };
    }
}
