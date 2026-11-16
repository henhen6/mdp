package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 指纹登录验证入参
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Accessors(chain = true)
@Schema(title = "FingerprintLoginVerifyDto", description = "指纹登录验证入参")
public class FingerprintLoginVerifyDto {

    @Schema(description = "登录挑战ID")
    @NotBlank(message = "挑战ID不能为空")
    private String challengeId;

    @Schema(description = "WebAuthn认证响应JSON")
    @NotBlank(message = "认证响应不能为空")
    private String responseJson;

    @Schema(description = "此次登录的客户端设备id")
    private String deviceId;

    @Schema(description = "设备信息")
    private String deviceInfo;
}
