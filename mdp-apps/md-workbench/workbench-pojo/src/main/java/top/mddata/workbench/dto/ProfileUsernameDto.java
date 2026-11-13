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
 * 个人中心-修改用户名（登录账号）
 * @author henhen6
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "个人中心-修改用户名")
public class ProfileUsernameDto {

    /**
     * 用户名
     */
    @Size(max = 255, message = "用户名长度不能超过{max}")
    @NotEmpty(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    /**
     * 当前密码（用于身份确认）
     */
    @NotEmpty(message = "当前密码不能为空")
    @Schema(description = "当前密码")
    private String password;

}