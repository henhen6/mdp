package top.mddata.workbench.oauth2.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.mddata.base.annotation.web.ParamName;

/**
 * Oauth2认证确认参数
 * @author henhen6
 * @since 2025/8/29 16:06
 */
@Data
@Schema(title = "ConfirmDto", description = "Oauth2认证确认参数")
// 自动将驼峰转换下划线
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ConfirmDto {
    /**
     * 应用id
     */
    @Schema(description = "应用id")
    @NotEmpty(message = "应用id不能为空")
    @ParamName("client_id")
    private String clientId;

    /**
     * 授权范围
     */
    @Schema(description = "授权范围")
    @NotEmpty(message = "授权范围不能为空")
    private String scope;

    @Schema(description = "是否构建重定向地址")
    @NotNull(message = "是否构建重定向地址不能为空")
    @ParamName("build_redirect_uri")
    private Boolean buildRedirectUri;
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
}
