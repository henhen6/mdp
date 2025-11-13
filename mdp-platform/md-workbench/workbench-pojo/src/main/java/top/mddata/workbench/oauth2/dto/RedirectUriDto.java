package top.mddata.workbench.oauth2.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import top.mddata.base.annotation.web.ParamName;

/**
 * Oauth2获取重定向地址
 * @author henhen6
 * @since 2025/8/29 16:06
 */
@Data
@Schema(title = "RedirectUriDto", description = "Oauth2获取重定向地址")
// 自动将驼峰转换下划线
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RedirectUriDto extends BaseClientDto {
//    /**
//     * 应用id
//     */
//    @Schema(description = "应用id")
//    @NotEmpty(message = "应用id不能为空")
//    private String clientId;
    /**
     * 授权类型, 非必填
     */
    @Schema(description = "授权类型")
    @NotEmpty(message = "授权类型不能为空")
    @ParamName("response_type")
    private String responseType;
    /**
     * 待重定向URL
     */
    @Schema(description = "重定向地址")
    @NotEmpty(message = "重定向地址不能为空")
    @ParamName("redirect_uri")
    private String redirectUri;

    /**
     * 状态标识, 可为null
     */
    @Schema(description = "状态标识")
    private String state;

    /**
     * 随机数
     */
    @Schema(description = "随机数")
    private String nonce;
    /**
     * 授权范围
     */
    @Schema(description = "授权范围")
    private String scope;

}
