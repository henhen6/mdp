package top.mddata.console.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
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
 * 角色资源关联 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:29
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色资源关联")
public class RoleResourceRelDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属角色
     */
    @NotNull(message = "请填写所属角色")
    @Schema(description = "所属角色")
    private Long roleId;

    /**
     * 资源类型
     */
    @NotEmpty(message = "请填写资源类型")
    @Size(max = 255, message = "资源类型长度不能超过{max}")
    @Schema(description = "资源类型")
    private String resourceType;

    /**
     * 所属资源
     */
    @NotNull(message = "请填写所属资源")
    @Schema(description = "所属资源")
    private Long resourceId;

}
