package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 微信授权换绑手机号 入参
 *
 * @author tangyh
 * @since 2026/10/10
 */
@Data
@Schema(title = "WxMaPhoneBindDto", description = "微信授权换绑手机号")
public class WxMaPhoneBindDto {

    /**
     * 手机号快速验证组件（getPhoneNumber）返回的动态令牌，用于换取用户手机号
     */
    @Schema(description = "getPhoneNumber 返回的 code")
    @NotEmpty(message = "手机号授权凭证phoneCode不能为空")
    private String phoneCode;
}
