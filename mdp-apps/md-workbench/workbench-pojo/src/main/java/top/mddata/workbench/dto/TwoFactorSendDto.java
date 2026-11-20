package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 双因素认证发送验证码 入参（匿名段，凭 ticket）
 *
 * @author tangyh
 */
@Data
@Schema(title = "TwoFactorSendDto", description = "双因素认证发送验证码入参")
public class TwoFactorSendDto {

    @Schema(description = "登录第一步返回的一次性票据")
    @NotBlank(message = "票据不能为空")
    private String ticket;

    @Schema(description = "验证渠道，仅开通渠道为 BOTH 时可传 PHONE/EMAIL 切换，不传默认手机优先")
    private String channel;
}
