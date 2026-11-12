package top.mddata.workbench.service.social;

import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthRequest;
import org.junit.jupiter.api.Test;
import top.mddata.workbench.config.SocialLoginProperties;
import top.mddata.workbench.enumeration.SocialProviderEnum;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link JustAuthRequestFactory} 单测。
 *
 * @author tangyh
 */
class JustAuthRequestFactoryTest {

    private final JustAuthRequestFactory factory = new JustAuthRequestFactory();

    @Test
    void Gitee授权地址保留完整hash回调() {
        String redirectUri = "http://localhost:7700/#/auth/social/callback/gitee";
        AuthRequest request = factory.create(SocialProviderEnum.GITEE, config(redirectUri));

        String authorizeUrl = request.authorize("test-state");

        assertEquals(redirectUri, queryParam(authorizeUrl, "redirect_uri"));
    }

    @Test
    void Gitee令牌地址保留完整hash回调且不修改原配置() throws ReflectiveOperationException {
        String redirectUri = "http://localhost:7700/#/auth/social/callback/gitee";
        SocialLoginProperties.ProviderConfig config = config(redirectUri);
        AuthRequest request = factory.create(SocialProviderEnum.GITEE, config);

        String tokenUrl = accessTokenUrl(request);

        assertEquals(redirectUri, queryParam(tokenUrl, "redirect_uri"));
        assertEquals(redirectUri, config.getRedirectUri());
    }

    @Test
    void Gitee普通回调地址保持原样() throws ReflectiveOperationException {
        String redirectUri = "https://example.com/auth/social/callback/gitee";
        AuthRequest request = factory.create(SocialProviderEnum.GITEE, config(redirectUri));

        assertEquals(redirectUri, queryParam(request.authorize("test-state"), "redirect_uri"));
        assertEquals(redirectUri, queryParam(accessTokenUrl(request), "redirect_uri"));
    }

    private SocialLoginProperties.ProviderConfig config(String redirectUri) {
        SocialLoginProperties.ProviderConfig config = new SocialLoginProperties.ProviderConfig();
        config.setClientId("test-client-id");
        config.setClientSecret("test-secret");
        config.setRedirectUri(redirectUri);
        return config;
    }

    private String accessTokenUrl(AuthRequest request) throws ReflectiveOperationException {
        Method method = AuthDefaultRequest.class.getDeclaredMethod("accessTokenUrl", String.class);
        method.setAccessible(true);
        return (String) method.invoke(request, "test-code");
    }

    private String queryParam(String url, String name) {
        String query = URI.create(url).getRawQuery();
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .filter(parts -> parts.length == 2 && parts[0].equals(name))
                .map(parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8))
                .findFirst()
                .orElseThrow();
    }
}
