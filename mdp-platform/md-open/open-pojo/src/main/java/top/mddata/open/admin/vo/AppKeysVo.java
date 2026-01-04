package top.mddata.open.admin.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.open.admin.entity.base.AppKeysBase;

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
@Schema(description = "应用秘钥Vo")
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


    @Schema(description = "订阅的事件")
    private List<Long> eventTypeIdList;

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
