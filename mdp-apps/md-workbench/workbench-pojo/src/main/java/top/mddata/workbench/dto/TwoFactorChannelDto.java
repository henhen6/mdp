package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;

/**
 * 双因素认证渠道 入参（需登录，仅发送开通/换渠道验证码时使用）。
 *
 * <p>与 {@link TwoFactorEnableDto} 区分：本入参场景不携带验证码，
 * 不能复用带 {@code @NotBlank} 校验的开通入参。</p>
 *
 * @author tangyh
 */
@Data
@Schema(title = "TwoFactorChannelDto", description = "双因素认证渠道入参")
public class TwoFactorChannelDto {

    @Schema(description = "验证渠道 PHONE/EMAIL/BOTH")
    @NotNull(message = "验证渠道不能为空")
    private TwoFactorChannelEnum channel;
}
