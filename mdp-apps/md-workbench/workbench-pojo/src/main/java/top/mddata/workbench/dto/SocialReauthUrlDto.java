package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.mddata.workbench.enumeration.SocialActionEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;

/**
 * 通过第三方重新授权完成二次验证时，请求授权地址的入参
 *
 * @author tangyh
 */
@Data
@Schema(title = "SocialReauthUrlDto", description = "第三方重新授权地址入参")
public class SocialReauthUrlDto {

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
}