package top.mddata.workbench.oauth2.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *Oauth2认证确认返回
 * @author henhen6
 * @since 2025/8/29 16:07
 */
@Data
@Schema(title = "ConfirmVo", description = "Oauth2认证确认返回")
// 自动将驼峰转换下划线
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ConfirmVo {

    private String redirectUri;
}
