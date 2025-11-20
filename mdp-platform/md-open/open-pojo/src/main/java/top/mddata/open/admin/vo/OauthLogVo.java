package top.mddata.open.admin.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.open.admin.entity.base.OauthLogBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 授权记录 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "授权记录")
@Table(OauthLogBase.TABLE_NAME)
public class OauthLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属应用
     */
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 所属用户
     */
    @Schema(description = "所属用户")
    private Long userId;

    /**
     * 开放id
     */
    @Schema(description = "开放id")
    private String openid;

    /**
     * 联合id
     */
    @Schema(description = "联合id")
    private String unionid;

    /**
     * 本次授权的token
     */
    @Schema(description = "本次授权的token")
    private String accessToken;

    /**
     * 到期时间
     */
    @Schema(description = "到期时间")
    private LocalDateTime accessTokenExpires;

    /**
     * 本次授权的刷新token
     */
    @Schema(description = "本次授权的刷新token")
    private String refreshToken;

    /**
     * 刷新token到期时间
     */
    @Schema(description = "刷新token到期时间")
    private LocalDateTime refreshTokenExpires;

    /**
     * 授权类型
     */
    @Schema(description = "授权类型")
    private String grantType;

    /**
     * 授权范围
     */
    @Schema(description = "授权范围")
    private String scopes;

    /**
     * 类型
     */
    @Schema(description = "类型")
    private String tokenType;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remarks;

    /**
     * 授权地址
     */
    @Schema(description = "授权地址")
    private String redirectUri;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

}
