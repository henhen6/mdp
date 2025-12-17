package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 手机 + 短信验证码 登录  入参
 *
 * @author henhen6
 * @since 2025/6/30 12:52
 */
@Data
@Schema(title = "MobileLoginDTO", description = "手机验证码登录")
public class PhoneLoginDto {
    @Schema(description = "验证码KEY")
    @NotEmpty(message = "请填写验证码")
    private String key;
    @NotEmpty(message = "请填写验证码")
    @Schema(description = "验证码")
    private String code;

    @Schema(description = "手机号")
    @NotEmpty(message = "请填写手机号")
    private String mobile;

}
