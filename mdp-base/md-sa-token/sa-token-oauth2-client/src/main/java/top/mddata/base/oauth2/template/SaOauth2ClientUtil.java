package top.mddata.base.oauth2.template;

import top.mddata.base.oauth2.processor.SaOauth2ClientProcessor;

/**
 * Oauth2 模板方法类 （Client端）
 * @author henhen6
 * @since 2025/9/3 20:54
 */
public class SaOauth2ClientUtil {

    /**
     * 构建URL：Server端 Oauth2登录授权地址，
     * <br/> 形如：{@code http://{host}:{port}/oauth2/authorize?response_type=code&client_id={client_id}&redirect_uri={redirect_uri}&scope={scope}&state={state}}
     * @param clientLoginUrl Client端登录地址
     * @param scope 权限范围
     * @param state 随机值
     * @return [Oauth2-Server端-认证地址 ]
     */
    public static String buildServerAuthorizeUrl(String clientLoginUrl, String scope, String state) {
        return SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate().buildServerAuthorizeUrl(clientLoginUrl, scope, state);
    }

    /**
     * 根据授权码换取 access_token（授权码模式）。
     * <br/> 调用Server端 {@code POST /oauth2/token}，grant_type=authorization_code
     *
     * @param code 授权码（一次性，有效期短）
     * @param redirectUri 重定向地址（必须与授权时传入的一致，可为null）
     * @return Server端响应的 JSON 字符串（含 access_token、refresh_token、openid 等字段，注意为 snake_case）
     */
    public static String getAccessTokenByCode(String code, String redirectUri) {
        return SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate().getAccessTokenByCode(code, redirectUri);
    }

    /**
     * 根据 access_token 获取用户信息。
     * <br/> 调用Server端 {@code POST /oauth2/userinfo}（要求 scope 含 userinfo）
     *
     * @param accessToken 访问令牌
     * @return Server端响应的 JSON 字符串（含用户昵称、头像、邮箱、手机号等公开信息）
     */
    public static String getUserInfoByAccessToken(String accessToken) {
        return SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate().getUserInfoByAccessToken(accessToken);
    }

    /**
     * 根据 refresh_token 刷新 access_token。
     * <br/> 调用Server端 {@code POST /oauth2/refresh}，grant_type=refresh_token
     *
     * @param refreshToken 刷新令牌
     * @return Server端响应的 JSON 字符串（含新的 access_token、refresh_token）
     */
    public static String refreshAccessToken(String refreshToken) {
        return SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate().refreshAccessToken(refreshToken);
    }

    /**
     * 回收 access_token，使其立即失效。
     * <br/> 调用Server端 {@code POST /oauth2/revoke}
     * <p> 建议在用户退出登录时调用，防止token在有效期内被继续使用
     *
     * @param accessToken 访问令牌
     * @return Server端响应的 JSON 字符串
     */
    public static String revokeAccessToken(String accessToken) {
        return SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate().revokeAccessToken(accessToken);
    }

    /**
     * 获取 client_token（凭证式，代表应用自身而非某个用户）。
     * <br/> 调用Server端 {@code POST /oauth2/client_token}，grant_type=client_credentials
     *
     * @param scope 权限范围
     * @return Server端响应的 JSON 字符串（含 client_token）
     */
    public static String getClientToken(String scope) {
        return SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate().getClientToken(scope);
    }

}
