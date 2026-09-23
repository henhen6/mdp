package top.mddata.base.oauth2.exception;

import lombok.Getter;

/**
 * OAuth2 Client端 调用异常
 * <p>
 * Server端返回标准错误响应（RFC 6749 §5.2：error + error_description）时抛出
 *
 * @author henhen6
 */
@Getter
public class Oauth2ClientException extends RuntimeException {

    /**
     * 标准错误码，见 {@code Oauth2ErrorConstants}
     */
    private final String error;

    /**
     * 错误描述
     */
    private final String errorDescription;

    public Oauth2ClientException(String error, String errorDescription) {
        super(error + ": " + errorDescription);
        this.error = error;
        this.errorDescription = errorDescription;
    }
}
