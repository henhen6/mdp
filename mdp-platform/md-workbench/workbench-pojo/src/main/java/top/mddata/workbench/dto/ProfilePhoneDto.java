package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 个人中心-手机信息
 * @author henhen6
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "个人中心-手机信息")
public class ProfilePhoneDto {
    @Size(max = 10, message = "发送序号长度不能超过{max}")
    @NotEmpty(message = "发送序号不能为空")
    @Schema(description = "发送序号")
    private String key;
    @Size(max = 6, message = "验证码长度不能超过{max}")
    @NotEmpty(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String code;

    /**
     * 手机号
     */
    @Size(max = 11, message = "原手机号长度不能超过{max}")
    @Schema(description = "原手机号")
    private String oldPhone;

    /**
     * 手机号
     */
    @Size(max = 11, message = "手机号长度不能超过{max}")
    @Schema(description = "手机号")
    private String phone;

}
