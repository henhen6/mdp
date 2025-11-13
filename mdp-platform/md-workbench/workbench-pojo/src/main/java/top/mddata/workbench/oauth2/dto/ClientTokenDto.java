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
@Schema(title = "ClientTokenDto", description = "回收token参数")
// 自动将驼峰转换下划线
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ClientTokenDto extends BaseClientDto {
    /**
     * 授权类型
     */
    @Schema(description = "授权类型")
    @NotEmpty(message = "授权类型不能为空")
    @ParamName("grant_type")
    private String grantType;
    /**
     * 授权范围
     * 逗号分割
     */
    @Schema(description = "授权范围")
    private String scope;

}
