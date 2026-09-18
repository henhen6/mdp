package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录后重定向参数  入参
 *
 * @author henhen6
 * @since 2025/6/30 12:52
 */
@Data
@Schema(title = "LoginRedirectUrlDto", description = "登录后重定向参数")
public class LoginRedirectUrlDto {

    @Schema(description = "AppKey")
    private String client;
    @Schema(description = "模式")
    private String mode;
    @Schema(description = "重定向地址")
    private String redirect;
}
