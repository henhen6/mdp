package top.mddata.open.admin.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用秘钥实体类。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
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
     * 秘钥格式
     * [1-PKCS8(JAVA适用) 2-PKCS1(非JAVA适用)]
     */
    private Integer keyFormat;

    /**
     * 应用公钥
     * 平台方用来校验开发者推送过来的数据
     */
    private String publicKeyApp;

    /**
     * 应用私钥
     * 一般由开发者自行生成或平台协助生成
     * 用来开发者签名推送给平台的数据
     */
    private String privateKeyApp;

    /**
     * 平台公钥
     * 提供给开发者，用来校验平台推送给开发者的数据签名是否正确
     */
    private String publicKeyPlatform;

    /**
     * 平台私钥
     * 平台使用，用来签名推送给开发者的数据
     */
    private String privateKeyPlatform;

}
