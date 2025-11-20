package top.mddata.open.admin.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * openid实体类。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OauthOpenidBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_oauth_openid";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属应用
     */
    private Long appId;

    /**
     * 所属用户
     */
    private Long userId;

    /**
     * openid
     */
    private String openid;

}
