package top.mddata.base.oauth2.core.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.mddata.base.annotation.web.ParamName;

/**
 * 令牌撤销请求参数（RFC 7009 §2.1）
 *
 * @author henhen6
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "Oauth2RevokeRequest", description = "令牌撤销请求参数")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Oauth2RevokeRequest extends Oauth2ClientCredentials {

    /**
     * 待撤销的令牌（access_token 或 refresh_token）
     */
    @Schema(description = "待撤销的令牌")
    @NotEmpty(message = "待撤销的令牌不能为空")
    @ParamName("token")
    private String token;

    /**
     * 令牌类型提示：access_token / refresh_token，可空
     */
    @Schema(description = "令牌类型提示")
    @ParamName("token_type_hint")
    private String tokenTypeHint;
}
