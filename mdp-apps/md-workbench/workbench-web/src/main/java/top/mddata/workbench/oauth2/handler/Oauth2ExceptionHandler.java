package top.mddata.workbench.oauth2.handler;

import cn.dev33.satoken.oauth2.error.SaOAuth2ErrorCode;
import cn.dev33.satoken.oauth2.exception.SaOAuth2Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.mddata.base.oauth2.core.constant.Oauth2ErrorConstants;
import top.mddata.base.oauth2.core.response.Oauth2ErrorResponse;
import top.mddata.workbench.oauth2.controller.OAuth2ResourceController;

import java.util.stream.Collectors;

/**
 * OAuth2 资源端点异常处理器
 * <p>
 * 仅作用于 {@link OAuth2ResourceController}，将异常转换为 RFC 6749 §5.2 / RFC 6750 §3.1
 * 标准错误格式（error + error_description + 合适的 HTTP 状态码），
 * 不影响 {@code OAuth2ServerController} 等内部接口继续使用 R&lt;&gt; 包装
 *
 * @author henhen6
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = OAuth2ResourceController.class)
public class Oauth2ExceptionHandler {

    private static final String REALM = "oauth2";

    /**
     * sa-token oauth2 业务异常 → 标准错误响应
     */
    @ExceptionHandler(SaOAuth2Exception.class)
    public ResponseEntity<Oauth2ErrorResponse> handleSaOAuth2Exception(SaOAuth2Exception e) {
        String error = mapError(e.getCode());
        HttpStatus status = mapStatus(error);
        log.warn("oauth2 请求失败: code={}, error={}, msg={}", e.getCode(), error, e.getMessage());

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);
        if (Oauth2ErrorConstants.INVALID_CLIENT.equals(error)) {
            // RFC 6749 §5.2：客户端认证失败需携带 Basic 质询
            builder.header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + REALM + "\"");
        } else if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
            // RFC 6750 §3：资源端点错误需携带 Bearer 质询
            builder.header(HttpHeaders.WWW_AUTHENTICATE, buildBearerChallenge(error, e.getMessage()));
        }
        return builder.body(Oauth2ErrorResponse.of(error, e.getMessage()));
    }

    /**
     * 表单参数校验失败 → invalid_request
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Oauth2ErrorResponse> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("oauth2 参数校验失败: {}", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Oauth2ErrorResponse.of(Oauth2ErrorConstants.INVALID_REQUEST, msg));
    }

    /**
     * 兜底：未预期异常 → server_error，保证第三方拿到的始终是标准错误格式
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Oauth2ErrorResponse> handleException(Exception e) {
        log.error("oauth2 端点内部错误", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Oauth2ErrorResponse.of(Oauth2ErrorConstants.SERVER_ERROR, "服务器内部错误"));
    }

    /**
     * sa-token 301xx 细分错误码 → 标准 error 字符串
     */
    private String mapError(int code) {
        return switch (code) {
            case SaOAuth2ErrorCode.CODE_30101, SaOAuth2ErrorCode.CODE_30105, SaOAuth2ErrorCode.CODE_30115 ->
                    Oauth2ErrorConstants.INVALID_CLIENT;
            case SaOAuth2ErrorCode.CODE_30102, SaOAuth2ErrorCode.CODE_30112 ->
                    Oauth2ErrorConstants.INVALID_SCOPE;
            case SaOAuth2ErrorCode.CODE_30106, SaOAuth2ErrorCode.CODE_30107 ->
                    Oauth2ErrorConstants.INVALID_TOKEN;
            case SaOAuth2ErrorCode.CODE_30108, SaOAuth2ErrorCode.CODE_30109 ->
                    Oauth2ErrorConstants.INSUFFICIENT_SCOPE;
            case SaOAuth2ErrorCode.CODE_30110, SaOAuth2ErrorCode.CODE_30111,
                 SaOAuth2ErrorCode.CODE_30120, SaOAuth2ErrorCode.CODE_30122,
                 SaOAuth2ErrorCode.CODE_30161 ->
                    Oauth2ErrorConstants.INVALID_GRANT;
            case SaOAuth2ErrorCode.CODE_30125 ->
                    Oauth2ErrorConstants.UNSUPPORTED_RESPONSE_TYPE;
            case SaOAuth2ErrorCode.CODE_30126 ->
                    Oauth2ErrorConstants.UNSUPPORTED_GRANT_TYPE;
            case SaOAuth2ErrorCode.CODE_30131, SaOAuth2ErrorCode.CODE_30132,
                 SaOAuth2ErrorCode.CODE_30133, SaOAuth2ErrorCode.CODE_30134,
                 SaOAuth2ErrorCode.CODE_30141, SaOAuth2ErrorCode.CODE_30142 ->
                    Oauth2ErrorConstants.UNAUTHORIZED_CLIENT;
            case SaOAuth2ErrorCode.CODE_30103, SaOAuth2ErrorCode.CODE_30113,
                 SaOAuth2ErrorCode.CODE_30114, SaOAuth2ErrorCode.CODE_30127,
                 SaOAuth2ErrorCode.CODE_30151, SaOAuth2ErrorCode.CODE_30191 ->
                    Oauth2ErrorConstants.INVALID_REQUEST;
            default -> Oauth2ErrorConstants.SERVER_ERROR;
        };
    }

    /**
     * 标准 error → HTTP 状态码
     */
    private HttpStatus mapStatus(String error) {
        return switch (error) {
            case Oauth2ErrorConstants.INVALID_CLIENT, Oauth2ErrorConstants.INVALID_TOKEN ->
                    HttpStatus.UNAUTHORIZED;
            case Oauth2ErrorConstants.INSUFFICIENT_SCOPE ->
                    HttpStatus.FORBIDDEN;
            case Oauth2ErrorConstants.SERVER_ERROR ->
                    HttpStatus.INTERNAL_SERVER_ERROR;
            case Oauth2ErrorConstants.TEMPORARILY_UNAVAILABLE ->
                    HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    /**
     * 构建 RFC 6750 规定的 Bearer 质询头
     */
    private String buildBearerChallenge(String error, String description) {
        StringBuilder sb = new StringBuilder("Bearer realm=\"").append(REALM).append("\"");
        sb.append(", error=\"").append(error).append("\"");
        if (description != null && !description.isEmpty()) {
            // error_description 需为 quoted-string，转义双引号
            sb.append(", error_description=\"").append(description.replace("\"", "\\\"")).append("\"");
        }
        return sb.toString();
    }
}
