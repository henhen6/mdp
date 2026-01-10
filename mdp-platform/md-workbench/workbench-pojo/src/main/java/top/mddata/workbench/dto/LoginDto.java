package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.mddata.workbench.enumeration.AuthTypeEnum;

/**
 * 登录信息  入参
 *
 * @author henhen6
 * @since 2025/6/30 12:52
 */
@Data
@Schema(title = "LoginDto", description = "登录信息")
public class LoginDto {

    /**
     * password: 账号密码
     * refresh_token: 刷新token
     * captcha: 验证码
     */
    @Schema(description = "授权类型", example = "CAPTCHA", allowableValues = "CAPTCHA,USERNAME,EMAIL,PHONE")
    @NotNull(message = "请选择正确的登录方式")
    private AuthTypeEnum authType;

    @Schema(description = "验证码KEY")
    private String key;
    @Schema(description = "验证码")
    private String code;

    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码")
    private String password;

    @Schema(description = "此次登录的客户端设备id")
    private String deviceId;
    /**
     * 前端获取
     */
    @Schema(description = "设备信息")
    private String deviceInfo;
}
