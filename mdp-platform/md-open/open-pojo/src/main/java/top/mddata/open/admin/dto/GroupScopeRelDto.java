package top.mddata.open.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分组拥有的oauth2权限 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分组拥有的oauth2权限")
public class GroupScopeRelDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 所属分组
     */
    @NotNull(message = "请填写所属分组")
    @Schema(description = "所属分组")
    private Long groupId;

    /**
     * 所属权限
     */
    @NotEmpty(message = "请填写所属权限")
    @Schema(description = "所属权限")
    private List<Long> scopeIdList;

}
