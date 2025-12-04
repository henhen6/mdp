package top.mddata.console.permission.dto;

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
 * 角色应用关联 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-03 14:54:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色应用关联")
public class RoleAppRelDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属角色
     */
    @NotNull(message = "请填写所属角色")
    @Schema(description = "所属角色")
    private Long roleId;

    /**
     * 所属应用
     */
    @NotEmpty(message = "请填写所属应用")
    @Schema(description = "所属应用")
    private List<Long> appIdList;

}
