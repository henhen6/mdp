package top.mddata.console.dto.permission;

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
     * 所属角色
     */
    @NotNull(message = "请填写角色所属角色", groups = BaseEntity.Update.class)
    @Schema(description = "所属角色")
    private Long roleId;
    /**
     * 编码
     */
    @Schema(description = "应用-资源集合")
    @NotNull(message = "应用资源集合不能为空")
    private Map<Long, List<Long>> appResourceMap;

    @Schema(description = "批量操作")
    private Boolean batch;
}
