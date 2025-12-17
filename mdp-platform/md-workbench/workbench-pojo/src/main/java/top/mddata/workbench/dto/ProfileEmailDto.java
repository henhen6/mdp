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
 * 个人中心-邮箱信息
 * @author henhen6
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "个人中心-邮箱信息")
public class ProfileEmailDto {
    @Size(max = 10, message = "发送序号长度不能超过{max}")
    @NotEmpty(message = "发送序号不能为空")
    @Schema(description = "发送序号")
    private String key;
    @Size(max = 6, message = "验证码长度不能超过{max}")
    @NotEmpty(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String code;

    /**
     * 原邮箱
     */
    @Size(max = 255, message = "原邮箱长度不能超过{max}")
    @Schema(description = "原邮箱")
    private String oldEmail;

    /**
     * 邮箱
     */
    @Size(max = 255, message = "邮箱长度不能超过{max}")
    @Schema(description = "邮箱")
    private String email;

}
