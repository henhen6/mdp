package top.mddata.console.permission.dto;

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
import java.util.List;
import java.util.Map;

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
    @NotNull(message = "请填写角色ID", groups = BaseEntity.Update.class)
    @Schema(description = "角色ID")
    private Long roleId;
    /**
     * 编码
     */
    @Schema(description = "应用-资源集合")
    @NotNull
    private Map<Long, List<Long>> appResourceMap;

}
