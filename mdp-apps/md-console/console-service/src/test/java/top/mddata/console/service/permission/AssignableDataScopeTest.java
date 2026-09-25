package top.mddata.console.service.permission;

import org.junit.jupiter.api.Test;
import top.mddata.base.mybatisflex.datapermission.DataScope;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 可分配数据范围档位规则测试
 * （spec 第 4 节：权限集合控制能配到哪一档）。
 */
class AssignableDataScopeTest {

    @Test
    void 权限集合为全部时可分配所有档含自定义() {
        List<DataScope> result = RoleService.getAssignableDataScopes(DataScope.ALL);
        assertEquals(DataScope.values().length, result.size());
        assertTrue(result.contains(DataScope.CUSTOM));
    }

    @Test
    void 权限集合为本公司及以下时不可分配全部与自定义() {
        List<DataScope> result = RoleService.getAssignableDataScopes(DataScope.COMPANY_AND_CHILD);
        assertFalse(result.contains(DataScope.ALL));
        assertFalse(result.contains(DataScope.CUSTOM));
        assertTrue(result.contains(DataScope.COMPANY_AND_CHILD));
        assertTrue(result.contains(DataScope.SELF));
    }

    @Test
    void 权限集合为仅本人时只能分配仅本人() {
        assertEquals(List.of(DataScope.SELF), RoleService.getAssignableDataScopes(DataScope.SELF));
    }

    @Test
    void 权限集合为空视为全部() {
        assertEquals(DataScope.values().length, RoleService.getAssignableDataScopes(null).size());
    }

    @Test
    void 权限集合为本部门时可分配本部门与仅本人() {
        assertEquals(List.of(DataScope.DEPT, DataScope.SELF),
                RoleService.getAssignableDataScopes(DataScope.DEPT));
    }

    @Test
    void 权限集合为自定义实现时从严只能分配仅本人() {
        assertEquals(List.of(DataScope.SELF),
                RoleService.getAssignableDataScopes(DataScope.CUSTOM));
    }
}
