package top.mddata.base.oauth2.name;

import lombok.Getter;

/**
 * Oauth2 模块所有参数名称定义
 * @author henhen6
 * @since 2025/9/3 23:19
 */
@Getter
public class ParamName {
    /** client 应用ID */
    private String clientId = "client_id";
    /**
     * 应用秘钥
     */
    private String clientSecret = "client_secret";
    /**
     * 返回类型
     */
    private String responseType = "response_type";
    /**
     *用户确认授权后，重定向的url地址
     */
    private String redirectUri = "redirect_uri";
    /**
     * 权限
     */
    private String scope = "scope";
    /**
     * 随机值， 此参数会在重定向时追加到url末尾
     */
    private String state = "state";
    /**
     * 授权类型
     */
    private String grantType = "grant_type";
    /**
     * 授权码（授权码模式下换取 access_token 的一次性凭证）
     */
    private String code = "code";
    /**
     * 访问令牌
     */
    private String accessToken = "access_token";
    /**
     * 刷新令牌
     */
    private String refreshToken = "refresh_token";
    /**
     * 是否立即构建 redirect_uri 授权地址
     */
    private String buildRedirectUri = "build_redirect_uri";

    public ParamName setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public ParamName setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public ParamName setResponseType(String responseType) {
        this.responseType = responseType;
        return this;
    }

    public ParamName setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    public ParamName setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public ParamName setState(String state) {
        this.state = state;
        return this;
    }

    public ParamName setGrantType(String grantType) {
        this.grantType = grantType;
        return this;
    }

    public ParamName setCode(String code) {
        this.code = code;
        return this;
    }

    public ParamName setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public ParamName setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public ParamName setBuildRedirectUri(String buildRedirectUri) {
        this.buildRedirectUri = buildRedirectUri;
        return this;
    }
}
