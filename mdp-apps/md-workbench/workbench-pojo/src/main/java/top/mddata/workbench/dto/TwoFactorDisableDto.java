package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 双因素认证关闭 入参（需登录）
 *
 * @author tangyh
 */
@Data
@Schema(title = "TwoFactorDisableDto", description = "双因素认证关闭入参")
public class TwoFactorDisableDto {

    @Schema(description = "当前渠道收到的验证码；开通渠道为 BOTH 时为手机验证码")
    @NotBlank(message = "验证码不能为空")
    private String code;

    @Schema(description = "邮箱验证码，开通渠道为 BOTH 时必填")
    private String emailCode;
}
