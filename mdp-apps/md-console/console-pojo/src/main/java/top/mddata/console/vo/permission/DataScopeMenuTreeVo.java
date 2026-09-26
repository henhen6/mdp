package top.mddata.console.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限菜单树节点 VO。
 *
 * <p>树中包含两类节点：dataScopeState=1 的可配置节点（带可分配档位），
 * 以及为展示树结构而补全的祖先节点（仅展示，不可配置）。</p>
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Data
@Schema(description = "数据权限菜单树节点")
public class DataScopeMenuTreeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单id")
    private Long menuId;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "所属应用")
    private Long appId;

    @Schema(description = "父级菜单id")
    private Long parentId;

    @Schema(description = "顺序号")
    private Integer weight;

    /**
     * 是否可配置：true=dataScopeState=1 的节点（前端展示档位下拉）；
     * false=祖先展示节点（仅展示，不可配置）
     */
    @Schema(description = "是否可配置")
    private Boolean configurable;

    /**
     * 可分配档位（仅 configurable=true 时非空，已按权限集合过滤、优先级降序）
     */
    @Schema(description = "可分配档位")
    private List<ScopeOption> scopes = new ArrayList<>();

    @Schema(description = "子节点")
    private List<DataScopeMenuTreeVo> children = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "档位选项")
    public static class ScopeOption implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "档位编码")
        private String code;

        @Schema(description = "档位名称")
        private String name;
    }
}
