package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 指纹录入挑战出参
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Accessors(chain = true)
@Schema(title = "FingerprintRegisterOptionsVo", description = "指纹录入挑战出参")
public class FingerprintRegisterOptionsVo {

    @Schema(description = "挑战ID，verify 时原样回传")
    private String challengeId;

    @Schema(description = "WebAuthn PublicKeyCredentialCreationOptions JSON")
    private String optionsJson;
}
