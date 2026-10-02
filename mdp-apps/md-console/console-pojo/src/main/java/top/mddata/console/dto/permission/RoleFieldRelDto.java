package top.mddata.console.dto.permission;

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
 * 角色字段受限关系 DTO（授权保存入参）。
 *
 * @author henhen6
 * @since 2026-10-02
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色字段受限关系")
public class RoleFieldRelDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @NotNull(message = "请填写角色ID")
    @Schema(description = "角色ID")
    private Long roleId;

    /**
     * 受限字段规则ID集合（mdc_resource_field.id），空集合表示清空该角色的字段限制
     */
    @Schema(description = "受限字段规则ID集合")
    private List<Long> fieldIdList;

}
