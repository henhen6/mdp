package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 忘记密码-凭验证码重置（手机号或邮箱 + 短信/邮箱验证码）
 *
 * @author tangyh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Schema(title = "ForgetPasswordByCodeDto", description = "忘记密码-验证码方式重置密码")
public class ForgetPasswordByCodeDto {
    @Schema(description = "接收账号（手机号或邮箱）")
    @NotEmpty(message = "请填写手机号或邮箱")
    private String account;

    @Schema(description = "验证码")
    @NotEmpty(message = "请填写验证码")
    private String code;

    @Schema(description = "密码")
    @NotEmpty(message = "请填写密码")
    @Size(min = 6, max = 64, message = "密码长度不能小于{min}且超过{max}个字符")
    private String password;

    @Schema(description = "确认密码")
    @NotEmpty(message = "请填写确认密码")
    @Size(min = 6, max = 64, message = "确认密码长度不能小于{min}且超过{max}个字符")
    private String confirmPassword;
}
