package top.mddata.console.dto.organization;

import io.swagger.v3.oas.annotations.media.Schema;
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
 * 用户-角色全量覆盖保存 DTO（用户侧分配角色入参）。
 *
 * @author henhen6
 * @since 2026-10-27
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户角色保存")
public class UserRoleSaveDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属用户
     */
    @NotNull(message = "请填写所属用户")
    @Schema(description = "所属用户")
    private Long userId;

    /**
     * 角色ID集合（空集合 = 清空该用户的可分配角色）
     */
    @Schema(description = "角色ID集合")
    private List<Long> roleIdList;

}
