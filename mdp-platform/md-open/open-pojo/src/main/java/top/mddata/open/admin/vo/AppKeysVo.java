package top.mddata.open.admin.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.open.admin.entity.base.AppKeysBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用秘钥 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    /**
     * 秘钥格式
     * [1-PKCS8(JAVA适用) 2-PKCS1(非JAVA适用)]
     */
    @Schema(description = "秘钥格式")
    private Integer keyFormat;

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
