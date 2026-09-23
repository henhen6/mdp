package top.mddata.base.oauth2.core.constant;

/**
 * OAuth2 协议常量（grant_type、token_type、token_type_hint）
 *
 * @author henhen6
 */
public interface Oauth2Constants {

    /** 授权码模式 */
    String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    /** 密码模式 */
    String GRANT_TYPE_PASSWORD = "password";

    /** 客户端凭证模式 */
    String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    /** 刷新令牌 */
    String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    /** 隐藏式（OAuth 2.1 已废弃，仅兼容旧客户端） */
    String GRANT_TYPE_IMPLICIT = "implicit";

    /** 授权端点 response_type：授权码 */
    String RESPONSE_TYPE_CODE = "code";

    /** 授权端点 response_type：隐藏式 */
    String RESPONSE_TYPE_TOKEN = "token";

    /** Bearer 令牌类型 */
    String TOKEN_TYPE_BEARER = "Bearer";

    /** token_type_hint：访问令牌 */
    String TOKEN_TYPE_HINT_ACCESS_TOKEN = "access_token";

    /** token_type_hint：刷新令牌 */
    String TOKEN_TYPE_HINT_REFRESH_TOKEN = "refresh_token";
}
