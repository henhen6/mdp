package top.mddata.base.oauth2.core.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.mddata.base.annotation.web.ParamName;

/**
 * 令牌端点请求参数（RFC 6749 §4.1.3 / §4.3.2 / §4.4.2 / §6）
 * <p>
 * 一个类覆盖 authorization_code、password、client_credentials、refresh_token 四种 grant_type，
 * 各模式按需取值，与标准令牌端点的表单参数保持一致
 *
 * @author henhen6
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "Oauth2TokenRequest", description = "令牌端点请求参数")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Oauth2TokenRequest extends Oauth2ClientCredentials {

    /**
     * 授权模式
     */
    @Schema(description = "授权模式")
    @NotEmpty(message = "授权模式不能为空")
    @ParamName("grant_type")
    private String grantType;

    /**
     * 授权码（authorization_code 模式必填）
     */
    @Schema(description = "授权码")
    @ParamName("code")
    private String code;

    /**
     * 重定向地址（authorization_code 模式下需与授权时一致）
     */
    @Schema(description = "重定向地址")
    @ParamName("redirect_uri")
    private String redirectUri;

    /**
     * 刷新令牌（refresh_token 模式必填）
     */
    @Schema(description = "刷新令牌")
    @ParamName("refresh_token")
    private String refreshToken;

    /**
     * 用户名（password 模式必填）
     */
    @Schema(description = "用户名")
    @ParamName("username")
    private String username;

    /**
     * 密码（password 模式必填）
     */
    @Schema(description = "密码")
    @ParamName("password")
    private String password;

    /**
     * 授权范围，多个以空格或逗号分隔
     */
    @Schema(description = "授权范围")
    @ParamName("scope")
    private String scope;
}
