package top.mddata.base.oauth2.core.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 令牌端点标准响应（RFC 6749 §5.1）
 * <p>
 * 同时服务于 authorization_code、password、refresh_token、client_credentials 四种模式，
 * client_credentials 模式下 refresh_token 为空将被忽略
 *
 * @author henhen6
 */
@Data
@Schema(title = "Oauth2TokenResponse", description = "令牌端点标准响应")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Oauth2TokenResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌")
    private String accessToken;

    /**
     * 令牌类型，固定为 Bearer
     */
    @Schema(description = "令牌类型")
    private String tokenType;

    /**
     * 访问令牌剩余有效期（秒）
     */
    @Schema(description = "访问令牌剩余有效期（秒）")
    private Long expiresIn;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌")
    private String refreshToken;

    /**
     * 刷新令牌剩余有效期（秒），扩展字段
     */
    @Schema(description = "刷新令牌剩余有效期（秒）")
    private Long refreshExpiresIn;

    /**
     * 应用id
     */
    @Schema(description = "应用id")
    private String clientId;

    /**
     * 授权范围
     */
    @Schema(description = "授权范围")
    private String scope;

    @Schema(description = "扩展参数")
    private Map<String, Object> extra = new LinkedHashMap<>();

    /**
     * 添加扩展属性（序列化时平铺到顶层，如 openid）
     *
     * @param key   属性名
     * @param value 属性值
     */
    @JsonAnySetter
    public void addExtra(String key, Object value) {
        this.extra.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getExtra() {
        return extra;
    }
}
