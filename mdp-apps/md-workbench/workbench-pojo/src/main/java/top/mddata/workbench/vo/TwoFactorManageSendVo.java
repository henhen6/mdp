package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 双因素管理验证码发送结果（安全设置：开通/换渠道）。
 * BOTH 渠道时手机、邮箱各发送一条，两个字段均有值；单渠道时仅对应字段有值。
 *
 * @author tangyh
 */
@Data
@Schema(title = "TwoFactorManageSendVo", description = "双因素管理验证码发送结果")
public class TwoFactorManageSendVo {

    @Schema(description = "脱敏手机号，未发送为 null")
    private String maskedPhone;

    @Schema(description = "脱敏邮箱，未发送为 null")
    private String maskedEmail;
}
