package top.mddata.workbench.oauth2.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.mddata.base.annotation.web.ParamName;

/**
 * Oauth2认证确认参数
 * @author henhen6
 * @since 2025/8/29 16:06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "RefreshDto", description = "Oauth2认证确认参数")
// 自动将驼峰转换下划线
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RefreshDto extends BaseClientDto {

    @Schema(description = "授权类型")
    @NotEmpty(message = "授权类型不能为空")
    @ParamName("grant_type")
    private String grantType;

    /**
     * 刷新 token
     */
    @Schema(description = "刷新token")
    @NotEmpty(message = "刷新token不能为空")
    @ParamName("refresh_token")
    private String refreshToken;


    @Schema(description = "授权范围")
    private String scope;

}
