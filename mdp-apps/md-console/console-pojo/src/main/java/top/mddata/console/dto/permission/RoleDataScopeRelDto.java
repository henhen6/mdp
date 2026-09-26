package top.mddata.console.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 角色数据范围授权 DTO。
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Data
@Schema(description = "角色数据范围授权")
public class RoleDataScopeRelDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "请填写角色")
    @Schema(description = "角色id")
    private Long roleId;

    /**
     * 授权项集合；空列表表示清空该角色的全部数据权限授权
     */
    @Valid
    @Schema(description = "授权项集合")
    private List<Item> items;

    @Data
    @Schema(description = "授权项")
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "请填写菜单")
        @Schema(description = "菜单id")
        private Long menuId;

        @NotBlank(message = "请填写数据范围档位")
        @Schema(description = "数据范围档位")
        private String dataScope;

        @Schema(description = "自定义实现的 Spring Bean 名（仅自定义档必填）")
        private String dataScopeImpl;
    }
}
