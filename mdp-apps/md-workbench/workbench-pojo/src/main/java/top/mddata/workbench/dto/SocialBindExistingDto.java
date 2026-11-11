package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 第三方身份绑定已有 MDP 账号入参
 *
 * @author tangyh
 */
@Data
@Schema(title = "SocialBindExistingDto", description = "绑定已有MDP账号入参")
public class SocialBindExistingDto {

    /** 第三方登录待选择票据 */
    @Schema(description = "待选择票据")
    @NotBlank(message = "待选择票据不能为空")
    private String pendingTicket;

    /** 已有账号凭据（用户名密码、手机或邮箱验证） */
    @Schema(description = "已有账号凭据")
    @Valid
    @NotNull(message = "账号凭据不能为空")
    private LoginDto login;
}
