package top.mddata.base.oauth2.core.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.mddata.base.annotation.web.ParamName;

import java.io.Serial;
import java.io.Serializable;

/**
 * 客户端凭证（RFC 6749 §2.3.1，支持请求参数或 Basic 头两种传递方式）
 *
 * @author henhen6
 */
@Data
@Schema(title = "Oauth2ClientCredentials", description = "客户端凭证")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Oauth2ClientCredentials implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用id
     */
    @Schema(description = "应用id")
    @ParamName("client_id")
    private String clientId;

    /**
     * 应用秘钥
     */
    @Schema(description = "应用秘钥")
    @ParamName("client_secret")
    private String clientSecret;
}
