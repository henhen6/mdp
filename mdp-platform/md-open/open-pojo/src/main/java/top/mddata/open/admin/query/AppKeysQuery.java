package top.mddata.open.admin.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用秘钥 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:14:29
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Schema(description = "应用秘钥Query")
public class AppKeysQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 所属应用
     */
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 秘钥格式
     * [1-PKCS8(JAVA适用) 2-PKCS1(非JAVA适用)]
     */
    @Schema(description = "秘钥格式")
    private Integer keyFormat;

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
     * 加密类型
     * [0-不加密 1-aes加密 2-sm4加密]
     */
    @Schema(description = "加密类型")
    private Integer notifyEncryptionType;

    /**
     * 应用公钥
     * 平台方用来校验开发者推送过来的数据
     */
    @Schema(description = "应用公钥")
    private String publicKeyApp;

    /**
     * 应用私钥
     * 一般由开发者自行生成或平台协助生成
     * 用来开发者签名推送给平台的数据
     */
    @Schema(description = "应用私钥")
    private String privateKeyApp;

    /**
     * 平台公钥
     * 提供给开发者，用来校验平台推送给开发者的数据签名是否正确
     */
    @Schema(description = "平台公钥")
    private String publicKeyPlatform;

    /**
     * 平台私钥
     * 平台使用，用来签名推送给开发者的数据
     */
    @Schema(description = "平台私钥")
    private String privateKeyPlatform;

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
