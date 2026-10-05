package top.mddata.console.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色数据权限授权页面聚合数据 VO。
 *
 * <p>按应用分组返回可配置的数据权限菜单树（含各节点可分配档位），
 * 应用全量展示（无可配置菜单的应用 menuTreeData 为空，前端面板内显示空态）。</p>
 *
 * @author henhen6
 * @since 2026-10-05
 */
@Data
@Schema(description = "角色数据权限授权页面聚合数据")
public class RoleDataScopeAuthVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用分组列表（角色已分配的应用，按应用权重降序）")
    private List<AppGroup> appGroupList = new ArrayList<>();

    @Schema(description = "可配置节点平铺列表（前端初始化与保存遍历用）")
    private List<MenuBrief> configurableMenus = new ArrayList<>();

    @Schema(description = "角色已授权的数据范围列表")
    private List<RoleDataScopeRelVo> authorizedList = new ArrayList<>();

    @Data
    @Schema(description = "应用分组")
    public static class AppGroup implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "应用id")
        private Long appId;

        @Schema(description = "应用名称")
        private String appName;

        @Schema(description = "该应用下的数据权限菜单树")
        private List<DataScopeMenuTreeVo> menuTreeData = new ArrayList<>();
    }

    @Data
    @Schema(description = "可配置菜单摘要")
    public static class MenuBrief implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "菜单id")
        private Long menuId;

        @Schema(description = "菜单名称")
        private String name;
    }
}
