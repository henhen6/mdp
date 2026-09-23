package top.mddata.base.oauth2.core.constant;

/**
 * OAuth2 / OIDC 标准错误码（RFC 6749 §5.2、RFC 6750 §3.1）
 *
 * @author henhen6
 */
public interface Oauth2ErrorConstants {

    /** 请求缺少必需参数、参数格式错误 */
    String INVALID_REQUEST = "invalid_request";

    /** 客户端认证失败（client_id / client_secret 错误） */
    String INVALID_CLIENT = "invalid_client";

    /** 授权许可无效（code、refresh_token、用户名密码错误等） */
    String INVALID_GRANT = "invalid_grant";

    /** 客户端未被授权使用此授权模式 */
    String UNAUTHORIZED_CLIENT = "unauthorized_client";

    /** 不支持的 grant_type */
    String UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";

    /** 不支持的 response_type */
    String UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";

    /** 请求的 scope 无效、未知或超出签约范围 */
    String INVALID_SCOPE = "invalid_scope";

    /** access_token 过期、被撤销或格式错误（RFC 6750） */
    String INVALID_TOKEN = "invalid_token";

    /** token 不具备访问该资源所需的 scope（RFC 6750） */
    String INSUFFICIENT_SCOPE = "insufficient_scope";

    /** 授权服务器内部错误 */
    String SERVER_ERROR = "server_error";

    /** 服务暂时不可用 */
    String TEMPORARILY_UNAVAILABLE = "temporarily_unavailable";
}
