package top.mddata.workbench.oauth2.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.mddata.base.annotation.web.ParamName;

/**
 * 回收token参数
 * @author henhen6
 * @since 2025/8/29 16:06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "RevokeDto", description = "回收token参数")
// 自动将驼峰转换下划线
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RevokeDto extends BaseClientDto {
    /**
     * Token
     */
    @Schema(description = "Token")
    @NotEmpty(message = "Token不能为空")
    @ParamName("access_token")
    private String accessToken;

}
