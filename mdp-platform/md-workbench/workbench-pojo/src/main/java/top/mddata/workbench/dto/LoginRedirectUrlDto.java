package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.mddata.workbench.enumeration.AuthTypeEnum;

/**
 * 登录后重定向参数  入参
 *
 * @author henhen6
 * @since 2025/6/30 12:52
 */
@Data
@Schema(title = "LoginRedirectUrlDto", description = "登录后重定向参数")
public class LoginRedirectUrlDto {
    /**
     * password: 账号密码
     * refresh_token: 刷新token
     * captcha: 验证码
     */
    @Schema(description = "授权类型", example = "CAPTCHA", allowableValues = "CAPTCHA,USERNAME,EMAIL,PHONE")
    @NotNull(message = "请选择正确的登录方式")
    private AuthTypeEnum authType;
    @Schema(description = "用户名")
    private String username;
    /**
     * 前端获取
     */
    @Schema(description = "设备信息")
    private String deviceInfo;

    @Schema(description = "AppKey")
    private String client;
    @Schema(description = "模式")
    private String mode;
    @Schema(description = "重定向地址")
    private String redirect;
}
