package top.mddata.base.oauth2.template;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.util.SaFoxUtil;
import lombok.Getter;
import top.mddata.base.oauth2.SaOauth2ClientManager;
import top.mddata.base.oauth2.name.ParamName;
import top.mddata.base.oauth2.properties.Oauth2ClientConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Oauth2 模板方法类 （Client端）
 * @author henhen6
 * @since 2025/9/3 20:58
 */
@Getter
public class SaOauth2ClientTemplate {
    /**
     * 所有参数名称
     */
    private ParamName paramName = new ParamName();

    public SaOauth2ClientTemplate setParamName(ParamName paramName) {
        this.paramName = paramName;
        return this;
    }

    /**
     构建URL：Server端 Oauth2登录授权地址，
     * <br/> 形如：{@code http://{host}:{port}/oauth2/authorize?response_type=code&client_id={client_id}&redirect_uri={redirect_uri}&scope={scope}&state={state}}
     * @param clientLoginUrl Client端登录地址
     * @param scope 权限范围
     * @param state 随机值
     * @return [SSO-Server端-认证地址 ]
     */
    public String buildServerAuthorizeUrl(String clientLoginUrl, String scope, String state) {
        Oauth2ClientConfig clientConfig = getClientConfig();
        // 服务端认证地址
        String serverUrl = clientConfig.splicingAuthorizeUrl();

        // 拼接 response_type
        serverUrl = SaFoxUtil.joinParam(serverUrl, paramName.getResponseType(), "code");

        // 拼接客户端标识
        String clientId = clientConfig.getClientId();
        serverUrl = SaFoxUtil.joinParam(serverUrl, paramName.getClientId(), clientId);

        // 重定向地址
        serverUrl = SaFoxUtil.joinParam(serverUrl, paramName.getRedirectUri(), SaFoxUtil.encodeUrl(clientLoginUrl));

        // scope
        if (scope != null && !scope.isEmpty()) {
            serverUrl = SaFoxUtil.joinParam(serverUrl, paramName.getScope(), scope);
        }

        // state
        if (state != null && !state.isEmpty()) {
            serverUrl = SaFoxUtil.joinParam(serverUrl, paramName.getState(), state);
        }

        return serverUrl;
    }

    /**
     * 根据授权码换取 access_token（授权码模式）。
     * <br/> 调用Server端 {@code POST /oauth2/token}，grant_type=authorization_code
     *
     * @param code 授权码（一次性，有效期短）
     * @param redirectUri 重定向地址（必须与授权时传入的一致，可为null）
     * @return Server端响应的 JSON 字符串（含 access_token、refresh_token、openid 等字段，注意为 snake_case）
     */
    public String getAccessTokenByCode(String code, String redirectUri) {
        Oauth2ClientConfig clientConfig = getClientConfig();
        Map<String, Object> params = new HashMap<>();
        params.put(paramName.getGrantType(), "authorization_code");
        params.put(paramName.getClientId(), clientConfig.getClientId());
        params.put(paramName.getClientSecret(), clientConfig.getClientSecret());
        if (SaFoxUtil.isNotEmpty(redirectUri)) {
            params.put(paramName.getRedirectUri(), redirectUri);
        }
        params.put(paramName.getCode(), code);
        return sendPost(clientConfig.splicingTokenUrl(), params);
    }

    /**
     * 根据 access_token 获取用户信息。
     * <br/> 调用Server端 {@code POST /oauth2/userinfo}（要求 scope 含 userinfo）
     *
     * @param accessToken 访问令牌
     * @return Server端响应的 JSON 字符串（含用户昵称、头像、邮箱、手机号等公开信息）
     */
    public String getUserInfoByAccessToken(String accessToken) {
        Oauth2ClientConfig clientConfig = getClientConfig();
        Map<String, Object> params = new HashMap<>();
        params.put(paramName.getAccessToken(), accessToken);
        return sendPost(clientConfig.splicingUserinfoUrl(), params);
    }

    /**
     * 根据 refresh_token 刷新 access_token。
     * <br/> 调用Server端 {@code POST /oauth2/refresh}，grant_type=refresh_token
     *
     * @param refreshToken 刷新令牌
     * @return Server端响应的 JSON 字符串（含新的 access_token、refresh_token）
     */
    public String refreshAccessToken(String refreshToken) {
        Oauth2ClientConfig clientConfig = getClientConfig();
        Map<String, Object> params = new HashMap<>();
        params.put(paramName.getGrantType(), "refresh_token");
        params.put(paramName.getClientId(), clientConfig.getClientId());
        params.put(paramName.getClientSecret(), clientConfig.getClientSecret());
        params.put(paramName.getRefreshToken(), refreshToken);
        return sendPost(clientConfig.splicingRefreshUrl(), params);
    }

    /**
     * 回收 access_token，使其立即失效。
     * <br/> 调用Server端 {@code POST /oauth2/revoke}
     * <p> 建议在用户退出登录时调用，防止token在有效期内被继续使用
     *
     * @param accessToken 访问令牌
     * @return Server端响应的 JSON 字符串
     */
    public String revokeAccessToken(String accessToken) {
        Oauth2ClientConfig clientConfig = getClientConfig();
        Map<String, Object> params = new HashMap<>();
        params.put(paramName.getClientId(), clientConfig.getClientId());
        params.put(paramName.getClientSecret(), clientConfig.getClientSecret());
        params.put(paramName.getAccessToken(), accessToken);
        return sendPost(clientConfig.splicingRevokeUrl(), params);
    }

    /**
     * 获取 client_token（凭证式，代表应用自身而非某个用户）。
     * <br/> 调用Server端 {@code POST /oauth2/client_token}，grant_type=client_credentials
     *
     * @param scope 权限范围
     * @return Server端响应的 JSON 字符串（含 client_token）
     */
    public String getClientToken(String scope) {
        Oauth2ClientConfig clientConfig = getClientConfig();
        Map<String, Object> params = new HashMap<>();
        params.put(paramName.getGrantType(), "client_credentials");
        params.put(paramName.getClientId(), clientConfig.getClientId());
        params.put(paramName.getClientSecret(), clientConfig.getClientSecret());
        if (SaFoxUtil.isNotEmpty(scope)) {
            params.put(paramName.getScope(), scope);
        }
        return sendPost(clientConfig.splicingClientTokenUrl(), params);
    }

    /**
     * 获取底层使用的SsoClient配置对象
     * @return /
     */
    public Oauth2ClientConfig getClientConfig() {
        return SaOauth2ClientManager.getClientConfig();
    }

    /**
     * 发送 POST 表单请求。
     * <p> 底层使用 SaManager 的 SaHttpTemplate，由集成方注入具体实现
     * （如 sa-token-forest 插件会自动注册 Forest 实现），与 SSO-Client 的 http 请求机制保持一致
     *
     * @param url 请求地址
     * @param params 请求参数
     * @return 响应体字符串
     */
    private String sendPost(String url, Map<String, Object> params) {
        return SaManager.getSaHttpTemplate().postByFormData(url, params);
    }
}
