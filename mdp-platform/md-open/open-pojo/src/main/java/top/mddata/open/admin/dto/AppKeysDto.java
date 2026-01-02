package top.mddata.open.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用秘钥 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:14:29
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "应用秘钥Dto")
public class AppKeysDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "请填写id", groups = BaseEntity.Update.class)
    @Schema(description = "id")
    private Long id;

    /**
     * 所属应用
     */
    @NotNull(message = "请填写所属应用")
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 秘钥格式
     * [1-PKCS8(JAVA适用) 2-PKCS1(非JAVA适用)]
     */
    @NotNull(message = "请填写秘钥格式")
    @Schema(description = "秘钥格式")
    private Integer keyFormat;

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
    private Boolean notifyState;

    /**
     * 加密类型
     * [0-不加密 1-aes加密 2-sm4加密]
     */
    @Schema(description = "加密类型")
    private Integer notifyEncryptionType;

    /**
     * 应用公钥
     * 平台方用来校验开发者推送过来的数据
     */
    @NotEmpty(message = "请填写应用公钥")
    @Size(max = 16383, message = "应用公钥长度不能超过{max}")
    @Schema(description = "应用公钥")
    private String publicKeyApp;

    /**
     * 应用私钥
     * 一般由开发者自行生成或平台协助生成
     * 用来开发者签名推送给平台的数据
     */
    @NotEmpty(message = "请填写应用私钥")
    @Size(max = 16383, message = "应用私钥长度不能超过{max}")
    @Schema(description = "应用私钥")
    private String privateKeyApp;

    /**
     * 平台公钥
     * 提供给开发者，用来校验平台推送给开发者的数据签名是否正确
     */
    @NotEmpty(message = "请填写平台公钥")
    @Size(max = 16383, message = "平台公钥长度不能超过{max}")
    @Schema(description = "平台公钥")
    private String publicKeyPlatform;

    /**
     * 平台私钥
     * 平台使用，用来签名推送给开发者的数据
     */
    @NotEmpty(message = "请填写平台私钥")
    @Size(max = 16383, message = "平台私钥长度不能超过{max}")
    @Schema(description = "平台私钥")
    private String privateKeyPlatform;

}
