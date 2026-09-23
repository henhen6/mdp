package top.mddata.base.oauth2.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 错误端点标准响应（RFC 6749 §5.2）
 *
 * @author henhen6
 */
@Data
@Schema(title = "Oauth2ErrorResponse", description = "错误端点标准响应")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Oauth2ErrorResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标准错误码，见 {@link top.mddata.base.oauth2.core.constant.Oauth2ErrorConstants}
     */
    @Schema(description = "标准错误码")
    private String error;

    /**
     * 错误描述（面向开发者，非终端用户）
     */
    @Schema(description = "错误描述")
    private String errorDescription;

    /**
     * 错误说明文档地址
     */
    @Schema(description = "错误说明文档地址")
    private String errorUri;

    /**
     * 构建错误响应
     *
     * @param error            标准错误码
     * @param errorDescription 错误描述
     * @return 错误响应
     */
    public static Oauth2ErrorResponse of(String error, String errorDescription) {
        Oauth2ErrorResponse response = new Oauth2ErrorResponse();
        response.setError(error);
        response.setErrorDescription(errorDescription);
        return response;
    }
}
