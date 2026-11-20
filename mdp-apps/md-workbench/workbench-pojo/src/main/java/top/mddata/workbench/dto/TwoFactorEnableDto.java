package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;

/**
 * 双因素认证开通/更换渠道 入参（需登录）
 *
 * @author tangyh
 */
@Data
@Schema(title = "TwoFactorEnableDto", description = "双因素认证开通入参")
public class TwoFactorEnableDto {

    @Schema(description = "验证渠道 PHONE/EMAIL/BOTH")
    @NotNull(message = "验证渠道不能为空")
    private TwoFactorChannelEnum channel;

    @Schema(description = "发送到新渠道的验证码；BOTH 时为手机验证码")
    @NotBlank(message = "验证码不能为空")
    private String code;

    @Schema(description = "邮箱验证码，channel 为 BOTH 时必填")
    private String emailCode;
}
