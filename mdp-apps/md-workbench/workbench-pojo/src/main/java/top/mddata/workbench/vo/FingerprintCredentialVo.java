package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 指纹凭证出参
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Accessors(chain = true)
@Schema(title = "FingerprintCredentialVo", description = "指纹凭证出参")
public class FingerprintCredentialVo {

    @Schema(description = "凭证ID")
    private Long id;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "认证器AAGUID")
    private String aaguid;

    @Schema(description = "登记时间")
    private LocalDateTime registeredAt;

    @Schema(description = "最后使用时间")
    private LocalDateTime lastUsedAt;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;
}
