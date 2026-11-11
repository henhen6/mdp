package top.mddata.workbench.service.social.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.exception.BizException;
import top.mddata.workbench.config.SocialLoginProperties;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.social.model.SocialIdentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link JustAuthSocialProvider} 适配器单测。
 *
 * <p>核心约束：微信、QQ、钉钉从 {@link AuthToken} 提取 openId，Gitee 使用
 * {@link AuthUser#getUuid()}；密钥不得进入异常与日志。</p>
 *
 * @author tangyh
 */
class JustAuthSocialProviderTest {

    private SocialLoginProperties.ProviderConfig config;
    private AuthRequest authRequest;
    private JustAuthSocialProvider provider;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        config = new SocialLoginProperties.ProviderConfig();
        config.setClientId("client-id");
        config.setClientSecret("test-secret");
        config.setRedirectUri("https://example.com/callback");
        authRequest = mock(AuthRequest.class);
        provider = new JustAuthSocialProvider(SocialProviderEnum.QQ, config, authRequest);

        Logger logger = (Logger) LoggerFactory.getLogger(JustAuthSocialProvider.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        ((Logger) LoggerFactory.getLogger(JustAuthSocialProvider.class)).detachAppender(logAppender);
    }

    @Test
    void authorize透传state并返回授权地址() {
        when(authRequest.authorize("state-1")).thenReturn("https://open.example.com/authorize");

        String url = provider.authorize("state-1");

        assertEquals("https://open.example.com/authorize", url);
        verify(authRequest).authorize("state-1");
    }

    @Test
    void hash回调地址编码后保留后续授权参数() {
        config.setRedirectUri("http://localhost:7700/#/auth/social/callback/gitee");
        when(authRequest.authorize("state-1")).thenReturn(
                "https://gitee.com/oauth/authorize?response_type=code"
                        + "&redirect_uri=http://localhost:7700/#/auth/social/callback/gitee"
                        + "&state=state-1&scope=user_info");

        String url = provider.authorize("state-1");

        assertEquals("https://gitee.com/oauth/authorize?response_type=code"
                + "&redirect_uri=http%3A%2F%2Flocalhost%3A7700%2F%23%2Fauth%2Fsocial%2Fcallback%2Fgitee"
                + "&state=state-1&scope=user_info", url);
    }

    @Test
    void exchange成功时从token提取openId和unionId() {
        AuthUser user = AuthUser.builder()
                .uuid("ignored-uuid")
                .nickname("测试用户")
                .token(AuthToken.builder().openId("open-id").unionId("union-id").build())
                .build();
        when(authRequest.login(any(AuthCallback.class)))
                .thenReturn(success(user));

        SocialIdentity identity = provider.exchange("code", "state");

        assertEquals(SocialProviderEnum.QQ, identity.getProvider());
        assertEquals("open-id", identity.getOpenId());
        assertEquals("union-id", identity.getUnionId());
        assertEquals("client-id", identity.getClientId());
        assertEquals("测试用户", identity.getNickname());
    }

    @Test
    void QQ启用UnionId时仍以tokenOpenId作为应用身份() {
        AuthUser user = AuthUser.builder()
                .uuid("union-id")
                .nickname("测试用户")
                .token(AuthToken.builder().openId("open-id").unionId("union-id").build())
                .build();
        when(authRequest.login(any(AuthCallback.class)))
                .thenReturn(success(user));

        SocialIdentity identity = provider.exchange("code", "state");

        assertEquals("open-id", identity.getOpenId());
        assertEquals("union-id", identity.getUnionId());
        assertEquals("client-id", identity.getClientId());
    }

    @Test
    void Gitee从AuthUserUuid提取用户标识() {
        provider = new JustAuthSocialProvider(SocialProviderEnum.GITEE, config, authRequest);
        AuthUser user = AuthUser.builder()
                .uuid("gitee-user-id")
                .nickname("Gitee用户")
                .token(AuthToken.builder().accessToken("test-token").build())
                .build();
        when(authRequest.login(any(AuthCallback.class)))
                .thenReturn(success(user));

        SocialIdentity identity = provider.exchange("code", "state");

        assertEquals(SocialProviderEnum.GITEE, identity.getProvider());
        assertEquals("gitee-user-id", identity.getOpenId());
        assertEquals("Gitee用户", identity.getNickname());
    }

    @Test
    void 昵称为空时回退为平台名称() {
        AuthUser user = AuthUser.builder()
                .token(AuthToken.builder().openId("open-id").build())
                .build();
        when(authRequest.login(any(AuthCallback.class)))
                .thenReturn(success(user));

        SocialIdentity identity = provider.exchange("code", "state");

        assertEquals(SocialProviderEnum.QQ.getLabel(), identity.getNickname());
    }

    @Test
    void 授权响应失败时抛出业务异常且日志不含密钥() {
        AuthResponse<AuthUser> response = AuthResponse.<AuthUser>builder()
                .code(AuthResponseStatus.FAILURE.getCode())
                .msg("error")
                .build();
        when(authRequest.login(any(AuthCallback.class))).thenReturn(response);

        BizException exception = assertThrows(BizException.class,
                () -> provider.exchange("code", "state"));
        assertEquals(false, exception.getMessage().contains("test-secret"));
        assertEquals(false, logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("test-secret")));
    }

    @Test
    void 授权未返回用户时抛出业务异常() {
        AuthResponse<AuthUser> response = AuthResponse.<AuthUser>builder()
                .code(AuthResponseStatus.SUCCESS.getCode())
                .build();
        when(authRequest.login(any(AuthCallback.class))).thenReturn(response);

        assertThrows(BizException.class, () -> provider.exchange("code", "state"));
    }

    @Test
    void QQ未返回tokenOpenId时不得回退uuid() {
        AuthUser user = AuthUser.builder()
                .uuid("uuid-only")
                .token(AuthToken.builder().unionId("union-id").build())
                .build();
        when(authRequest.login(any(AuthCallback.class)))
                .thenReturn(success(user));

        assertThrows(ArgumentException.class, () -> provider.exchange("code", "state"));
    }

    private AuthResponse<AuthUser> success(AuthUser user) {
        return AuthResponse.<AuthUser>builder()
                .code(AuthResponseStatus.SUCCESS.getCode())
                .data(user)
                .build();
    }
}
