package top.mddata.open.admin.dto;

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

/**
 * unionid DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "unionid")
public class OauthUnionidDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属主体
     */
    @Schema(description = "所属主体")
    private Long subjectId;

    /**
     * 所属用户
     */
    @Schema(description = "所属用户")
    private Long userId;

    /**
     * Unionid
     */
    @Size(max = 128, message = "Unionid长度不能超过{max}")
    @Schema(description = "Unionid")
    private String unionid;

}
