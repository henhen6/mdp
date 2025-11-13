package top.mddata.workbench.oauth2.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import top.mddata.base.annotation.web.ParamName;

/**
 * Oauth2认证确认参数
 * @author henhen6
 * @since 2025/8/29 16:06
 */
@Data
@Schema(title = "BaseClientDto", description = "基础信息")
// 自动将驼峰转换下划线
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BaseClientDto {


    /**
     * 应用id
     */
    @Schema(description = "应用id")
    @NotEmpty(message = "应用id不能为空")
    @ParamName("client_id")
    private String clientId;

    /**
     * 应用秘钥
     */
    @Schema(description = "应用秘钥")
    @ParamName("client_secret")
    private String clientSecret;
}
