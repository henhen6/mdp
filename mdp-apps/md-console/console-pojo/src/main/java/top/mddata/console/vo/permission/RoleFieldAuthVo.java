package top.mddata.console.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色字段权限授权页面聚合数据 VO。
 *
 * <p>按应用分组返回「菜单 + 字段规则」树，树中仅保留含字段规则的分支；
 * 应用全量展示（无字段规则的应用 menuTree 为空，前端面板内显示空态）。</p>
 *
 * @author henhen6
 * @since 2026-10-05
 */
@Data
@Schema(description = "角色字段权限授权页面聚合数据")
public class RoleFieldAuthVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用分组列表（角色已分配的应用，按应用权重降序）")
    private List<AppGroup> appGroupList = new ArrayList<>();

    @Data
    @Schema(description = "应用分组")
    public static class AppGroup implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "应用id")
        private Long appId;

        @Schema(description = "应用名称")
        private String appName;

        @Schema(description = "该应用下的菜单字段树（仅含字段规则的分支）")
        private List<MenuNode> menuTree = new ArrayList<>();

        @Schema(description = "该应用下已受限的字段规则id集合")
        private List<Long> checkedFieldIds = new ArrayList<>();
    }

    @Data
    @Schema(description = "菜单节点")
    public static class MenuNode implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "菜单id")
        private Long menuId;

        @Schema(description = "菜单名称")
        private String name;

        @Schema(description = "本菜单的字段规则")
        private List<FieldNode> fields = new ArrayList<>();

        @Schema(description = "子菜单")
        private List<MenuNode> children = new ArrayList<>();
    }

    @Data
    @Schema(description = "字段规则节点")
    public static class FieldNode implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "字段规则id")
        private Long fieldId;

        @Schema(description = "字段名称")
        private String name;

        @Schema(description = "实体类字段")
        private String property;

        @Schema(description = "处理动作（1-隐藏 2-脱敏）")
        private Integer ruleType;
    }
}
