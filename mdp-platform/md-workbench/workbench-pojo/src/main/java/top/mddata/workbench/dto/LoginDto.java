package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.mddata.workbench.enumeration.LoginType;

/**
 * 用户名登录  入参
 *
 * @author henhen6
 * @since 2025/6/30 12:52
 */
@Data
@Schema(title = "UsernameLoginDTO", description = "用户名登录")
public class LoginDto {

    /**
     * password: 账号密码
     * refresh_token: 刷新token
     * captcha: 验证码
     */
    @Schema(description = "授权类型", example = "CAPTCHA", allowableValues = "CAPTCHA,USERNAME,EMAIL,PHONE")
    @NotNull(message = "请选择正确的登录方式")
    private LoginType loginType;

    @Schema(description = "验证码KEY")
    private String key;
    @Schema(description = "验证码")
    private String code;

    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码")
    private String password;
}
