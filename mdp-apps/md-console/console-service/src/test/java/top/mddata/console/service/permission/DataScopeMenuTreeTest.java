package top.mddata.console.service.permission;

import org.junit.jupiter.api.Test;
import top.mddata.console.entity.permission.ResourceMenu;
import top.mddata.console.service.permission.impl.RoleDataScopeRelServiceImpl;
import top.mddata.console.vo.permission.DataScopeMenuTreeVo;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 数据权限菜单树组装测试：祖先解析与组树两个纯函数。
 */
class DataScopeMenuTreeTest {

    private static ResourceMenu menu(Long id, Long parentId, String treePath, Integer weight) {
        ResourceMenu menu = new ResourceMenu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setTreePath(treePath);
        menu.setWeight(weight);
        return menu;
    }

    private static DataScopeMenuTreeVo node(Long menuId, Long parentId, boolean configurable, int weight) {
        DataScopeMenuTreeVo node = new DataScopeMenuTreeVo();
        node.setMenuId(menuId);
        node.setParentId(parentId);
        node.setConfigurable(configurable);
        node.setWeight(weight);
        return node;
    }

    @Test
    void 解析祖先id_排除自身与已启用菜单() {
        List<ResourceMenu> enabled = List.of(
                menu(30L, 20L, "/1001/10/20/30/", 1),
                menu(40L, 30L, "/1001/10/20/30/40/", 1));
        Set<Long> ancestors = RoleDataScopeRelServiceImpl.parseAncestorIds(enabled);
        // 40 的祖先链含 30，但 30 本身是已启用菜单（可配置节点），不能重复补为展示节点
        assertEquals(Set.of(1001L, 10L, 20L), ancestors);
    }

    @Test
    void 组树_多级嵌套_共享祖先去重_按weight排序() {
        List<DataScopeMenuTreeVo> nodes = List.of(
                node(30L, 20L, true, 2),
                node(40L, 20L, true, 1),
                node(10L, null, false, 1),
                node(20L, 10L, false, 1));
        List<DataScopeMenuTreeVo> roots = RoleDataScopeRelServiceImpl.buildTree(nodes);

        assertEquals(1, roots.size());
        DataScopeMenuTreeVo root = roots.get(0);
        assertEquals(10L, root.getMenuId());

        DataScopeMenuTreeVo level2 = root.getChildren().get(0);
        assertEquals(20L, level2.getMenuId());
        assertEquals(2, level2.getChildren().size());
        // 按 weight 升序：40(w=1) 在 30(w=2) 前
        assertEquals(40L, level2.getChildren().get(0).getMenuId());
        assertEquals(30L, level2.getChildren().get(1).getMenuId());
    }

    @Test
    void 组树_父节点不在集合中则提升为根() {
        List<DataScopeMenuTreeVo> roots = RoleDataScopeRelServiceImpl.buildTree(
                List.of(node(30L, 20L, true, 1)));
        assertEquals(1, roots.size());
        assertEquals(30L, roots.get(0).getMenuId());
    }

    @Test
    void 组树_可配置节点也可有后代() {
        // 3 级可配置菜单挂在 2 级可配置菜单下（链上两个启用节点都保留 configurable 标记）
        List<DataScopeMenuTreeVo> nodes = List.of(
                node(20L, null, true, 1),
                node(30L, 20L, true, 1));
        List<DataScopeMenuTreeVo> roots = RoleDataScopeRelServiceImpl.buildTree(nodes);
        assertEquals(1, roots.size());
        assertEquals(Boolean.TRUE, roots.get(0).getConfigurable());
        assertEquals(Boolean.TRUE, roots.get(0).getChildren().get(0).getConfigurable());
    }
}
