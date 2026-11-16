package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 指纹登录挑战出参
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Accessors(chain = true)
@Schema(title = "FingerprintLoginOptionsVo", description = "指纹登录挑战出参")
public class FingerprintLoginOptionsVo {

    @Schema(description = "挑战ID，verify 时原样回传")
    private String challengeId;

    @Schema(description = "WebAuthn PublicKeyCredentialRequestOptions JSON")
    private String optionsJson;
}
