package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 微信小程序登录 入参
 *
 * @author tangyh
 * @since 2026/10/10
 */
@Data
@Schema(title = "WxMaLoginDto", description = "微信小程序登录")
public class WxMaLoginDto {

    /**
     * 小程序 wx.login() 返回的临时登录凭证
     */
    @Schema(description = "wx.login() 返回的 code")
    @NotEmpty(message = "登录凭证loginCode不能为空")
    private String loginCode;

    /**
     * 手机号快速验证组件（getPhoneNumber）返回的动态令牌，用于换取用户手机号。
     * 可选：个人主体小程序无该组件权限，不传时按 openid 静默注册（不绑定手机号）
     */
    @Schema(description = "getPhoneNumber 返回的 code（可选）")
    private String phoneCode;

    @Schema(description = "此次登录的客户端设备id")
    private String deviceId;

    @Schema(description = "设备信息")
    private String deviceInfo;
}
