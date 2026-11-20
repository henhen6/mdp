package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 双因素认证验证码校验 入参（匿名段）
 *
 * @author tangyh
 */
@Data
@Schema(title = "TwoFactorVerifyDto", description = "双因素认证验证码校验入参")
public class TwoFactorVerifyDto {

    @Schema(description = "登录第一步返回的一次性票据")
    @NotBlank(message = "票据不能为空")
    private String ticket;

    @Schema(description = "验证码")
    @NotBlank(message = "验证码不能为空")
    private String code;
}
