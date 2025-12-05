package top.mddata.console.organization.dto;

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
 * 用户角色关联 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户角色关联")
public class UserRoleRelDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属角色
     */
    @NotNull(message = "请填写所属角色")
    @Schema(description = "所属角色")
    private Long roleId;

    /**
     * 所属用户
     */
    @NotEmpty(message = "请填写所属用户")
    @Schema(description = "所属用户")
    private List<Long> userIdList;

}
