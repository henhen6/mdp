package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 指纹录入验证入参
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Accessors(chain = true)
@Schema(title = "FingerprintRegisterVerifyDto", description = "指纹录入验证入参")
public class FingerprintRegisterVerifyDto {

    @Schema(description = "录入挑战ID")
    @NotBlank(message = "挑战ID不能为空")
    private String challengeId;

    @Schema(description = "WebAuthn注册响应JSON")
    @NotBlank(message = "注册响应不能为空")
    private String responseJson;

    @Schema(description = "前端localStorage设备ID")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "平台 win32/darwin/linux")
    private String platform;
}
