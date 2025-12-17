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
 * 个人中心-修改密码
 * @author henhen6
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "个人中心-修改密码")
public class ProfilePasswordDto {

    /**
     * 原密码
     */
    @Schema(description = "原密码")
    @NotEmpty(message = "原密码不能为空")
    private String oldPassword;

    /**
     * 密码
     */
    @Size(min = 6, max = 20, message = "密码长度应为：{min}-{max}")
    @Schema(description = "密码")
    @NotEmpty(message = "密码不能为空")
    private String password;

    /**
     * 确认密码
     */
    @Size(min = 6, max = 20, message = "确认密码长度应为：{min}-{max}")
    @Schema(description = "确认密码")
    @NotEmpty(message = "确认密码不能为空")
    private String confirmPassword;

}
