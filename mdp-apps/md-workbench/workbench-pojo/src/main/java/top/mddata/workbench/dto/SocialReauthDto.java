package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.mddata.workbench.enumeration.SocialActionEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;

/**
 * 通过 MDP 账号凭据完成的二次验证入参
 *
 * @author tangyh
 */
@Data
@Schema(title = "SocialReauthDto", description = "账号凭据二次验证入参")
public class SocialReauthDto {

    /**
     * 待执行的敏感操作类型
     */
    @Schema(description = "敏感操作类型")
    @NotNull(message = "操作类型不能为空")
    private SocialActionEnum action;

    /**
     * 待操作的目标平台
     */
    @Schema(description = "目标平台")
    @NotNull(message = "目标平台不能为空")
    private SocialProviderEnum provider;

    /**
     * 当前 MDP 账号凭据
     */
    @Schema(description = "当前账号凭据")
    @Valid
    @NotNull(message = "账号凭据不能为空")
    private LoginDto login;
}