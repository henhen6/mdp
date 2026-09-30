package top.mddata.open.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 应用秘钥 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:14:29
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "应用秘钥")
public class AppKeysDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属应用
     */
    @NotNull(message = "请填写所属应用")
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 通知地址
     */
    @Size(max = 255, message = "通知地址长度不能超过{max}")
    @Schema(description = "通知地址")
    private String notifyUrl;

    /**
     * 通知状态
     */
    @Schema(description = "通知状态")
    @NotNull(message = "请填写通知状态")
    private Boolean notifyState;

    /**
     * 加密模式，[0-明文模式 1-兼容模式 2-安全模式]
     */
    @Schema(description = "加密模式")
    private Integer notifyEncryptionType;

    /**
     * 签名校验令牌，平台和开发者共同持有，用于生成和验证 signature / msg_signature
     */
    @Schema(description = "签名校验令牌，平台和开发者共同持有，用于生成和验证 signature / msg_signature")
    private String notifyToken;

    /**
     * AES加解密密钥（43字符），平台和开发者共同持有。生成方式：Base64Decode(encodingAesKey + "=") 得到32字节AESKey
     */
    @Schema(description = "AES加解密密钥（43字符），平台和开发者共同持有。生成方式：Base64Decode(encodingAesKey + \"=\") 得到32字节AESKey")
    private String notifyEncodingAesKey;

    /**
     * 开发者应用公钥（RSA2签名校验用）
     */
    @Schema(description = "开发者应用公钥（RSA2签名校验用）")
    private String publicKeyApp;

    @Schema(description = "订阅的事件")
    private List<Long> eventTypeIdList;
}
