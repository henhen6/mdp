package top.mddata.base.oauth2.template;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.json.SaJsonTemplate;
import cn.dev33.satoken.util.SaFoxUtil;
import lombok.Getter;
import top.mddata.base.oauth2.SaOauth2ClientManager;
import top.mddata.base.oauth2.core.constant.Oauth2Constants;
import top.mddata.base.oauth2.core.request.Oauth2RevokeRequest;
import top.mddata.base.oauth2.core.request.Oauth2TokenRequest;
import top.mddata.base.oauth2.core.response.Oauth2TokenResponse;
import top.mddata.base.oauth2.core.response.Oauth2UserInfoResponse;
import top.mddata.base.oauth2.exception.Oauth2ClientException;
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
     构建URL：Server端 Oauth2登录授权地址（授权码模式），
     * <br/> 形如：{@code http://{host}:{port}/oauth2/authorize?response_type=code&client_id={client_id}&redirect_uri={redirect_uri}&scope={scope}&state={state}}
     * @param clientLoginUrl Client端登录地址
     * @param scope 权限范围
     * @param state 随机值
     * @return [SSO-Server端-认证地址 ]
     */
    public String buildCodeAuthorizeUrl(String clientLoginUrl, String scope, String state) {
        return buildServerAuthorizeUrl(clientLoginUrl, scope, state, Oauth2Constants.RESPONSE_TYPE_CODE);
    }

    /**
     构建URL：Server端 Oauth2登录授权地址（隐藏式），
     * <br/> 形如：{@code http://{host}:{port}/oauth2/authorize?response_type=token&client_id={client_id}&redirect_uri={redirect_uri}&scope={scope}&state={state}}
     * <p> 隐藏式由Server端通过 URL 重定向直接下放 Access-Token，无需再调 token 端点换票。
     * 注意：该模式已被 OAuth 2.1 废弃，仅建议用于兼容旧客户端
     * @param clientLoginUrl Client端登录地址
     * @param scope 权限范围
     * @param state 随机值
     * @return [SSO-Server端-认证地址 ]
     */
    public String buildImplicitAuthorizeUrl(String clientLoginUrl, String scope, String state) {
        return buildServerAuthorizeUrl(clientLoginUrl, scope, state, Oauth2Constants.RESPONSE_TYPE_TOKEN);
    }

    /**
     构建URL：Server端 Oauth2登录授权地址，
     * <br/> 形如：{@code http://{host}:{port}/oauth2/authorize?response_type=code&client_id={client_id}&redirect_uri={redirect_uri}&scope={scope}&state={state}}
     * <p> 仅授权码（code）与隐藏式（token）两种模式经过授权端点；
     * 密码式直接调 {@code /oauth2/token}、客户端凭证直接调 {@code /oauth2/client_token}，不存在授权跳转地址。
     * 注意：隐藏式（response_type=token）已被 OAuth 2.1 废弃，仅建议用于兼容旧客户端
     * @param clientLoginUrl Client端登录地址
     * @param scope 权限范围
     * @param state 随机值
     * @param responseType 授权类型：{@code code}（授权码）或 {@code token}（隐藏式）
     * @return [SSO-Server端-认证地址 ]
     */
    public String buildServerAuthorizeUrl(String clientLoginUrl, String scope, String state, String responseType) {
        if (!Oauth2Constants.RESPONSE_TYPE_CODE.equals(responseType) && !Oauth2Constants.RESPONSE_TYPE_TOKEN.equals(responseType)) {
            throw new IllegalArgumentException("responseType 仅支持 code（授权码）或 token（隐藏式）：" + responseType);
        }
        Oauth2ClientConfig clientConfig = getClientConfig();
        // 服务端认证地址
        String serverUrl = clientConfig.splicingAuthorizeUrl();

        // 拼接 response_type
        serverUrl = SaFoxUtil.joinParam(serverUrl, paramName.getResponseType(), responseType);

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
     * @param request 令牌请求（code 必填，redirectUri 必须与授权时传入的一致，可为null）
     * @return 标准令牌响应（含 access_token、refresh_token、openid 等字段）
     */
    public Oauth2TokenResponse getAccessTokenByCode(Oauth2TokenRequest request) {
        if (SaFoxUtil.isEmpty(request.getCode())) {
            throw new IllegalArgumentException("code 不能为空");
        }
        Oauth2ClientConfig clientConfig = getClientConfig();
        Map<String, Object> params = buildClientCredentialParams(clientConfig);
        params.put(paramName.getGrantType(), Oauth2Constants.GRANT_TYPE_AUTHORIZATION_CODE);
        if (SaFoxUtil.isNotEmpty(request.getRedirectUri())) {
            params.put(paramName.getRedirectUri(), request.getRedirectUri());
        }
        params.put(paramName.getCode(), request.getCode());
        String json = sendPost(clientConfig.splicingTokenUrl(), params);
        return parseResponse(json, Oauth2TokenResponse.class);
    }

    /**
     * 根据用户名密码换取 access_token（密码模式）。
     * <br/> 调用Server端 {@code POST /oauth2/token}，grant_type=password
     * <p> 密码模式仅适用于高度信任的第一方客户端（OAuth 2.1 已废弃），且Server端需开启 enablePassword
     *
     * @param request 令牌请求（username、password 必填，scope 可空）
     * @return 标准令牌响应（含 access_token、refresh_token 等字段）
     */
    public Oauth2TokenResponse getAccessTokenByPassword(Oauth2TokenRequest request) {
        if (SaFoxUtil.isEmpty(request.getUsername())) {
            throw new IllegalArgumentException("username 不能为空");
        }
        if (SaFoxUtil.isEmpty(request.getPassword())) {
            throw new IllegalArgumentException("password 不能为空");
        }
        Oauth2ClientConfig clientConfig = getClientConfig();
        Map<String, Object> params = buildClientCredentialParams(clientConfig);
        params.put(paramName.getGrantType(), Oauth2Constants.GRANT_TYPE_PASSWORD);
        params.put(paramName.getUsername(), request.getUsername());
        params.put(paramName.getPassword(), request.getPassword());
        if (SaFoxUtil.isNotEmpty(request.getScope())) {
            params.put(paramName.getScope(), request.getScope());
        }
        String json = sendPost(clientConfig.splicingTokenUrl(), params);
        return parseResponse(json, Oauth2TokenResponse.class);
    }

    /**
     * 根据 access_token 获取用户信息。
     * <br/> 调用Server端 {@code POST /oauth2/userinfo}（要求 scope 含 userinfo）
     *
     * @param accessToken 访问令牌
     * @return 用户公开信息（含 sub、昵称、头像、邮箱、手机号等）
     */
    public Oauth2UserInfoResponse getUserInfoByAccessToken(String accessToken) {
        if (SaFoxUtil.isEmpty(accessToken)) {
            throw new IllegalArgumentException("accessToken 不能为空");
        }
        Oauth2ClientConfig clientConfig = getClientConfig();
        Map<String, Object> params = new HashMap<>();
        params.put(paramName.getAccessToken(), accessToken);
        String json = sendPost(clientConfig.splicingUserinfoUrl(), params);
        return parseResponse(json, Oauth2UserInfoResponse.class);
    }

    /**
     * 根据 refresh_token 刷新 access_token。
     * <br/> 调用Server端 {@code POST /oauth2/refresh}，grant_type=refresh_token
     *
     * @param request 令牌请求（refreshToken 必填，scope 可空，仅支持收窄授权范围）
     * @return 标准令牌响应（含新的 access_token、refresh_token）
     */
    public Oauth2TokenResponse refreshAccessToken(Oauth2TokenRequest request) {
        if (SaFoxUtil.isEmpty(request.getRefreshToken())) {
            throw new IllegalArgumentException("refreshToken 不能为空");
        }
        Oauth2ClientConfig clientConfig = getClientConfig();
        Map<String, Object> params = buildClientCredentialParams(clientConfig);
        params.put(paramName.getGrantType(), Oauth2Constants.GRANT_TYPE_REFRESH_TOKEN);
        params.put(paramName.getRefreshToken(), request.getRefreshToken());
        if (SaFoxUtil.isNotEmpty(request.getScope())) {
            params.put(paramName.getScope(), request.getScope());
        }
        String json = sendPost(clientConfig.splicingRefreshUrl(), params);
        return parseResponse(json, Oauth2TokenResponse.class);
    }

    /**
     * 回收 token，使其立即失效（RFC 7009）。
     * <br/> 调用Server端 {@code POST /oauth2/revoke}
     * <p> 建议在用户退出登录时调用，防止token在有效期内被继续使用；
     * 撤销 access_token 时Server端会级联撤销关联的 refresh_token
     *
     * @param request 撤销请求（token 必填，tokenTypeHint 可空）
     */
    public void revokeAccessToken(Oauth2RevokeRequest request) {
        if (SaFoxUtil.isEmpty(request.getToken())) {
            throw new IllegalArgumentException("token 不能为空");
        }
        Oauth2ClientConfig clientConfig = getClientConfig();
        Map<String, Object> params = buildClientCredentialParams(clientConfig);
        params.put(paramName.getToken(), request.getToken());
        if (SaFoxUtil.isNotEmpty(request.getTokenTypeHint())) {
            params.put(paramName.getTokenTypeHint(), request.getTokenTypeHint());
        }
        String json = sendPost(clientConfig.splicingRevokeUrl(), params);
        // RFC 7009：成功时响应体为空，仅当返回了错误体时才需要解析
        if (SaFoxUtil.isNotEmpty(json)) {
            checkErrorResponse(json);
        }
    }

    /**
     * 获取 client_token（凭证式，代表应用自身而非某个用户）。
     * <br/> 调用Server端 {@code POST /oauth2/client_token}，grant_type=client_credentials
     *
     * @param request 令牌请求（scope 可空）
     * @return 标准令牌响应（access_token 即 client_token）
     */
    public Oauth2TokenResponse getClientToken(Oauth2TokenRequest request) {
        Oauth2ClientConfig clientConfig = getClientConfig();
        Map<String, Object> params = buildClientCredentialParams(clientConfig);
        params.put(paramName.getGrantType(), Oauth2Constants.GRANT_TYPE_CLIENT_CREDENTIALS);
        if (SaFoxUtil.isNotEmpty(request.getScope())) {
            params.put(paramName.getScope(), request.getScope());
        }
        String json = sendPost(clientConfig.splicingClientTokenUrl(), params);
        return parseResponse(json, Oauth2TokenResponse.class);
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

    /**
     * 构建带客户端凭证的请求参数，clientId / clientSecret 统一取自本地配置
     */
    private Map<String, Object> buildClientCredentialParams(Oauth2ClientConfig clientConfig) {
        Map<String, Object> params = new HashMap<>();
        params.put(paramName.getClientId(), clientConfig.getClientId());
        params.put(paramName.getClientSecret(), clientConfig.getClientSecret());
        return params;
    }

    /**
     * 解析响应体：标准错误响应（{"error": ...}）转为异常抛出，正常响应反序列化为目标类型
     *
     * @param json 响应体
     * @param type 目标类型
     * @return 反序列化结果
     * @param <T> 目标类型泛型
     */
    private <T> T parseResponse(String json, Class<T> type) {
        checkErrorResponse(json);
        return SaManager.getSaJsonTemplate().jsonToObject(json, type);
    }

    /**
     * 检查响应体是否为标准错误格式，是则抛出异常
     */
    private void checkErrorResponse(String json) {
        SaJsonTemplate jsonTemplate = SaManager.getSaJsonTemplate();
        Map<String, Object> map = jsonTemplate.jsonToMap(json);
        Object error = map.get("error");
        if (error != null) {
            Object errorDescription = map.get("error_description");
            throw new Oauth2ClientException(String.valueOf(error), errorDescription == null ? null : String.valueOf(errorDescription));
        }
    }
}
