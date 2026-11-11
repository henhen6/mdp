package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.mddata.workbench.enumeration.SocialProviderEnum;

/**
 * 误注册后的跨账号安全转移入参
 *
 * <p>把误注册账号下绑定的第三方身份转移到用户已验证的已有账号，不迁移用户
 * 资料、组织、角色等任何业务数据。</p>
 *
 * @author tangyh
 */
@Data
@Schema(title = "SocialTransferDto", description = "跨账号安全转移入参")
public class SocialTransferDto {

    /**
     * 待转移的第三方平台
     */
    @Schema(description = "待转移平台")
    @NotNull(message = "待转移平台不能为空")
    private SocialProviderEnum provider;

    /**
     * 二次验证票据
     */
    @Schema(description = "二次验证票据")
    @NotBlank(message = "二次验证票据不能为空")
    private String reauthTicket;

    /**
     * 目标账号凭据（用户名密码、手机或邮箱验证）
     */
    @Schema(description = "目标账号凭据")
    @Valid
    @NotNull(message = "目标账号凭据不能为空")
    private LoginDto targetLogin;
}