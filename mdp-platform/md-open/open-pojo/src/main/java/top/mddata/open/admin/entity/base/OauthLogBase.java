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
import java.time.LocalDateTime;

/**
 * 授权记录实体类。
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
public class OauthLogBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_oauth_log";

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
     * 开放id
     */
    private String openid;

    /**
     * 联合id
     */
    private String unionid;

    /**
     * 本次授权的token
     */
    private String accessToken;

    /**
     * 到期时间
     */
    private LocalDateTime accessTokenExpires;

    /**
     * 本次授权的刷新token
     */
    private String refreshToken;

    /**
     * 刷新token到期时间
     */
    private LocalDateTime refreshTokenExpires;

    /**
     * 授权类型
     */
    private String grantType;

    /**
     * 授权范围
     */
    private String scopes;

    /**
     * 类型
     */
    private String tokenType;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 授权地址
     */
    private String redirectUri;

}
