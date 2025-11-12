package top.mddata.console.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户所属组织 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户所属组织")
public class UserOrgRelDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属用户
     */
    @NotNull(message = "请填写所属用户")
    @Schema(description = "所属用户")
    private Long userId;

    /**
     * 所属组织
     */
    @NotNull(message = "请填写所属组织")
    @Schema(description = "所属组织")
    private Long orgId;

}
