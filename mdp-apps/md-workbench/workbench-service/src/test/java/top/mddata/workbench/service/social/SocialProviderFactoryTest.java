package top.mddata.workbench.service.social;

import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthDingTalkV2Request;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthQqRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import org.junit.jupiter.api.Test;
import top.mddata.base.exception.BizException;
import top.mddata.workbench.config.SocialLoginProperties;
import top.mddata.workbench.enumeration.SocialProviderEnum;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SocialProviderFactory} 与 {@link JustAuthRequestFactory} 单测。
 *
 * <p>核心约束：启用但配置不完整必须 fail fast；平台与 JustAuth 请求类型一一对应，
 * 钉钉固定使用 V2 请求；每次获取都产生新的适配器实例。</p>
 *
 * @author tangyh
 */
class SocialProviderFactoryTest {

    @Test
    void 启用但配置不完整时构造工厂快速失败() {
        SocialLoginProperties properties = new SocialLoginProperties();
        properties.getQq().setEnabled(true);
        properties.getQq().setClientId("client-id");

        BizException exception = assertThrows(BizException.class,
                () -> new SocialProviderFactory(properties, new JustAuthRequestFactory()));
        assertTrue(exception.getMessage().contains(SocialProviderEnum.QQ.getLabel()));
    }

    @Test
    void 平台与JustAuth请求类型一一对应() {
        SocialLoginProperties properties = completeProperties();
        SocialProviderFactory factory = new SocialProviderFactory(properties, new JustAuthRequestFactory());

        Map<SocialProviderEnum, Class<? extends AuthDefaultRequest>> expects = Map.of(
                SocialProviderEnum.WECHAT_OPEN, AuthWeChatOpenRequest.class,
                SocialProviderEnum.QQ, AuthQqRequest.class,
                SocialProviderEnum.DINGTALK, AuthDingTalkV2Request.class,
                SocialProviderEnum.GITEE, AuthGiteeRequest.class);

        expects.forEach((provider, expectedType) -> {
            AuthRequest authRequest = authRequest(factory.get(provider));
            assertEquals(expectedType, authRequest.getClass());
            AuthConfig authConfig = authConfig(authRequest);
            assertTrue(authConfig.isIgnoreCheckState());
        });
    }

    @Test
    void 钉钉使用V2请求并携带可选corpId() {
        SocialLoginProperties properties = completeProperties();
        properties.getDingTalk().setCorpId("test-corp-id");
        SocialProviderFactory factory = new SocialProviderFactory(properties, new JustAuthRequestFactory());

        AuthRequest authRequest = authRequest(factory.get(SocialProviderEnum.DINGTALK));

        assertEquals(AuthDingTalkV2Request.class, authRequest.getClass());
        assertEquals("test-corp-id", authConfig(authRequest).getDingTalkCorpId());
    }

    @Test
    void http超时写入JustAuth配置() {
        SocialLoginProperties properties = completeProperties();
        properties.getQq().setHttpTimeoutMillis(3_000);
        SocialProviderFactory factory = new SocialProviderFactory(properties, new JustAuthRequestFactory());

        AuthConfig authConfig = authConfig(authRequest(factory.get(SocialProviderEnum.QQ)));

        assertEquals(Integer.valueOf(3_000), authConfig.getHttpConfig().getTimeout());
    }

    @Test
    void 每次获取创建新的适配器和请求对象() {
        SocialLoginProperties properties = completeProperties();
        SocialProviderFactory factory = new SocialProviderFactory(properties, new JustAuthRequestFactory());

        SocialProvider first = factory.get(SocialProviderEnum.QQ);
        SocialProvider second = factory.get(SocialProviderEnum.QQ);

        assertNotSame(first, second);
        assertNotSame(authRequest(first), authRequest(second));
    }

    @Test
    void 获取未启用平台时快速失败() {
        SocialLoginProperties properties = completeProperties();
        properties.getWechatOpen().setEnabled(false);
        SocialProviderFactory factory = new SocialProviderFactory(properties, new JustAuthRequestFactory());

        assertThrows(BizException.class, () -> factory.get(SocialProviderEnum.WECHAT_OPEN));
    }

    @Test
    void enabledProviders只返回启用且配置完整的平台() {
        SocialLoginProperties properties = completeProperties();
        properties.getWechatOpen().setEnabled(false);
        SocialProviderFactory factory = new SocialProviderFactory(properties, new JustAuthRequestFactory());

        assertEquals(3, factory.enabledProviders().size());
        assertTrue(factory.enabledProviders().contains(SocialProviderEnum.QQ));
        assertTrue(factory.enabledProviders().contains(SocialProviderEnum.DINGTALK));
        assertTrue(factory.enabledProviders().contains(SocialProviderEnum.GITEE));
    }

    private SocialLoginProperties completeProperties() {
        SocialLoginProperties properties = new SocialLoginProperties();
        properties.getWechatOpen().setEnabled(true);
        fill(properties.getWechatOpen());
        properties.getQq().setEnabled(true);
        fill(properties.getQq());
        properties.getDingTalk().setEnabled(true);
        fill(properties.getDingTalk());
        properties.getGitee().setEnabled(true);
        fill(properties.getGitee());
        return properties;
    }

    private void fill(SocialLoginProperties.ProviderConfig config) {
        config.setClientId("client-id");
        config.setClientSecret("test-secret");
        config.setRedirectUri("https://example.com/callback");
    }

    private AuthRequest authRequest(SocialProvider provider) {
        try {
            Field field = provider.getClass().getDeclaredField("authRequest");
            field.setAccessible(true);
            return (AuthRequest) field.get(provider);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private AuthConfig authConfig(AuthRequest authRequest) {
        try {
            Field field = AuthDefaultRequest.class.getDeclaredField("config");
            field.setAccessible(true);
            return (AuthConfig) field.get(authRequest);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
