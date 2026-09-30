package top.mddata.open.vo.admin;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.open.entity.admin.base.AppKeysBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用秘钥 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:14:29
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "应用秘钥")
@Table(AppKeysBase.TABLE_NAME)
public class AppKeysVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private Long id;

    /**
     * 所属应用
     */
    @Schema(description = "所属应用")
    private Long appId;
    @Schema(description = "所属应用标识")
    private String appKey;
    @Schema(description = "所属应用秘钥")
    private String appSecret;
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 通知地址
     */
    @Schema(description = "通知地址")
    private String notifyUrl;

    /**
     * 通知状态
     */
    @Schema(description = "通知状态")
    private Boolean notifyState;

    /**
     * 加密模式，[0-明文模式 1-兼容模式 2-安全模式]
     */
    @Schema(description = "加密模式，[0-明文模式 1-兼容模式 2-安全模式]")
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

    /**
     * 添加时间
     */
    @Schema(description = "添加时间")
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;

    /**
     * 创建人id
     */
    @Schema(description = "创建人id")
    private Long createdBy;

    /**
     * 修改人id
     */
    @Schema(description = "修改人id")
    private Long updatedBy;

}
