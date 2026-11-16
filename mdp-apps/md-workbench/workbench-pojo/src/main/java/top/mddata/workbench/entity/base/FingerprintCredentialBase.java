package top.mddata.workbench.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 指纹登录凭证实体类。
 *
 * @author tangyh
 * @since 2026-11-13
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FingerprintCredentialBase extends SuperEntity<Long> implements Serializable {
    /**
     * 表名称
     */
    public static final String TABLE_NAME = "mdw_fingerprint_credential";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * WebAuthn凭证ID(base64url)，全局唯一
     */
    private String credentialId;

    /**
     * COSE公钥(base64)
     */
    private String publicKey;

    /**
     * 签名计数器(防克隆)
     */
    private Long signCount;

    /**
     * 认证器AAGUID
     */
    private String aaguid;

    /**
     * 前端localStorage设备ID，仅辅助展示与统计
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 平台 win32/darwin/linux
     */
    private String platform;

    /**
     * 登记时User-Agent
     */
    private String userAgent;

    /**
     * 登记时间
     */
    private LocalDateTime registeredAt;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 删除用户
     */
    private Long deletedBy;

    /**
     * 删除标识
     */
    private Long deletedAt;
}
