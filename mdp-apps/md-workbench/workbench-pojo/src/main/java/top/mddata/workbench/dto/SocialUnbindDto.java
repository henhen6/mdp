package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.mddata.workbench.enumeration.SocialProviderEnum;

/**
 * 解绑第三方账号入参
 *
 * @author tangyh
 */
@Data
@Schema(title = "SocialUnbindDto", description = "解绑第三方账号入参")
public class SocialUnbindDto {

    /**
     * 待解绑的平台
     */
    @Schema(description = "待解绑平台")
    @NotNull(message = "解绑平台不能为空")
    private SocialProviderEnum provider;

    /**
     * 二次验证票据
     */
    @Schema(description = "二次验证票据")
    @NotBlank(message = "二次验证票据不能为空")
    private String reauthTicket;
}