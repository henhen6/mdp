package top.mddata.open.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 授权记录 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-09-10 23:54:49
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "授权记录")
public class OauthLogDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属应用
     */
    @Schema(description = "所属应用")
    private Long applicationId;

    /**
     * 所属用户
     */
    @Schema(description = "所属用户")
    private Long userId;

    /**
     * 开放id
     */
    @Size(max = 128, message = "开放id长度不能超过{max}")
    @Schema(description = "开放id")
    private String openid;

    /**
     * 联合id
     */
    @Size(max = 128, message = "联合id长度不能超过{max}")
    @Schema(description = "联合id")
    private String unionid;

    /**
     * 本次授权的token
     */
    @Size(max = 300, message = "本次授权的token长度不能超过{max}")
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
    @Size(max = 300, message = "本次授权的刷新token长度不能超过{max}")
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
    @Size(max = 50, message = "授权类型长度不能超过{max}")
    @Schema(description = "授权类型")
    private String grantType;

    /**
     * 授权范围
     */
    @Size(max = 1024, message = "授权范围长度不能超过{max}")
    @Schema(description = "授权范围")
    private String scopes;

    /**
     * 类型
     */
    @Size(max = 50, message = "类型长度不能超过{max}")
    @Schema(description = "类型")
    private String tokenType;

    /**
     * 备注
     */
    @Size(max = 1024, message = "备注长度不能超过{max}")
    @Schema(description = "备注")
    private String remarks;

    /**
     * 授权地址
     */
    @Size(max = 512, message = "授权地址长度不能超过{max}")
    @Schema(description = "授权地址")
    private String redirectUri;

}
