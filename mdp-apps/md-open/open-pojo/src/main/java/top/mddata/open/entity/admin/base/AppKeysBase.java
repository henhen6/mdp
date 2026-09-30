package top.mddata.open.entity.admin.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用秘钥实体类。
 *
 * @author henhen6
 * @since 2026-01-02 10:14:29
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AppKeysBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_app_keys";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属应用
     */
    private Long appId;

    /**
     * 通知地址
     */
    private String notifyUrl;

    /**
     * 通知状态
     */
    private Boolean notifyState;

    /**
     * 加密模式，[0-明文模式 1-兼容模式 2-安全模式]
     */
    private Integer notifyEncryptionType;

    /**
     * 签名校验令牌，平台和开发者共同持有，用于生成和验证 signature / msg_signature
     */
    private String notifyToken;

    /**
     * AES加解密密钥（43字符），平台和开发者共同持有。生成方式：Base64Decode(encodingAesKey + "=") 得到32字节AESKey
     * 平台和开发者共同持有，用于消息体的加密和解密
     */
    private String notifyEncodingAesKey;

    /**
     * 开发者应用公钥（RSA2签名校验用）
     * 开发者生成密钥对后将公钥上传至平台，用于验证开发者请求签名
     * 对应地，开发者使用私钥对请求参数进行签名
     */
    private String publicKeyApp;

}
