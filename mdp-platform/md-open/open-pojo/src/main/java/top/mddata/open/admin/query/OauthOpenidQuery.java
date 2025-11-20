package top.mddata.open.admin.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * openid Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "openid")
public class OauthOpenidQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
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
     * openid
     */
    @Schema(description = "openid")
    private String openid;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

}
