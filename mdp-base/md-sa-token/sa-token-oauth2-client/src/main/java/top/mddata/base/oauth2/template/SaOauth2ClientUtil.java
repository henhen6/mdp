package top.mddata.base.oauth2.template;

import top.mddata.base.oauth2.core.request.Oauth2RevokeRequest;
import top.mddata.base.oauth2.core.request.Oauth2TokenRequest;
import top.mddata.base.oauth2.core.response.Oauth2TokenResponse;
import top.mddata.base.oauth2.core.response.Oauth2UserInfoResponse;
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
     * @param request 令牌请求（code 必填，redirectUri 必须与授权时传入的一致，可为null）
     * @return 标准令牌响应（含 access_token、refresh_token、openid 等字段）
     */
    public static Oauth2TokenResponse getAccessTokenByCode(Oauth2TokenRequest request) {
        return SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate().getAccessTokenByCode(request);
    }

    /**
     * 根据 access_token 获取用户信息。
     * <br/> 调用Server端 {@code POST /oauth2/userinfo}（要求 scope 含 userinfo）
     *
     * @param accessToken 访问令牌
     * @return 用户公开信息（含 sub、昵称、头像、邮箱、手机号等）
     */
    public static Oauth2UserInfoResponse getUserInfoByAccessToken(String accessToken) {
        return SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate().getUserInfoByAccessToken(accessToken);
    }

    /**
     * 根据 refresh_token 刷新 access_token。
     * <br/> 调用Server端 {@code POST /oauth2/refresh}，grant_type=refresh_token
     *
     * @param request 令牌请求（refreshToken 必填，scope 可空，仅支持收窄授权范围）
     * @return 标准令牌响应（含新的 access_token、refresh_token）
     */
    public static Oauth2TokenResponse refreshAccessToken(Oauth2TokenRequest request) {
        return SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate().refreshAccessToken(request);
    }

    /**
     * 回收 token，使其立即失效（RFC 7009）。
     * <br/> 调用Server端 {@code POST /oauth2/revoke}
     * <p> 建议在用户退出登录时调用，防止token在有效期内被继续使用；
     * 撤销 access_token 时Server端会级联撤销关联的 refresh_token
     *
     * @param request 撤销请求（token 必填，tokenTypeHint 可空）
     */
    public static void revokeAccessToken(Oauth2RevokeRequest request) {
        SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate().revokeAccessToken(request);
    }

    /**
     * 获取 client_token（凭证式，代表应用自身而非某个用户）。
     * <br/> 调用Server端 {@code POST /oauth2/client_token}，grant_type=client_credentials
     *
     * @param request 令牌请求（scope 可空）
     * @return 标准令牌响应（access_token 即 client_token）
     */
    public static Oauth2TokenResponse getClientToken(Oauth2TokenRequest request) {
        return SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate().getClientToken(request);
    }

}
