package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 邮箱 + 短信验证码 登录  入参
 *
 * @author henhen6
 * @since 2025/6/30 12:52
 */
@Data
@Schema(title = "EmailLoginDTO", description = "邮箱验证码登录")
public class EmailLoginDto {
    @Schema(description = "验证码KEY")
    @NotEmpty(message = "请填写验证码")
    private String key;
    @Schema(description = "验证码")
    @NotEmpty(message = "请填写验证码")
    private String code;

    @Schema(description = "手机号")
    @NotEmpty(message = "请填写手机号")
    private String mobile;

}
