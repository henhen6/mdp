package top.mddata.console.service.permission;

import org.junit.jupiter.api.Test;
import top.mddata.base.mybatisflex.datascope.model.DataScopeEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 可分配档位计算测试：操作人可分配档位不得高于
 * 权限集合对该菜单的授权档（优先级语义）。
 */
class DataScopeAssignableTest {

    @Test
    void 权限集合未授权该菜单_不可分配() {
        assertTrue(RoleService.getAssignableDataScopes(null).isEmpty());
    }

    @Test
    void 权限集合为全部_六档全部分配() {
        List<DataScopeEnum> result = RoleService.getAssignableDataScopes(DataScopeEnum.ALL);
        assertEquals(6, result.size());
        assertEquals(DataScopeEnum.ALL, result.get(0));
    }

    @Test
    void 权限集合为自定义_可分配自定义及以下() {
        List<DataScopeEnum> result = RoleService.getAssignableDataScopes(DataScopeEnum.CUSTOM);
        assertFalse(result.contains(DataScopeEnum.ALL));
        assertTrue(result.contains(DataScopeEnum.CUSTOM));
        assertTrue(result.contains(DataScopeEnum.SELF));
    }

    @Test
    void 权限集合为本公司及以下_不可分配全部与自定义() {
        List<DataScopeEnum> result = RoleService.getAssignableDataScopes(
                DataScopeEnum.COMPANY_AND_CHILD);
        assertEquals(List.of(DataScopeEnum.COMPANY_AND_CHILD, DataScopeEnum.DEPT_AND_CHILD,
                DataScopeEnum.DEPT, DataScopeEnum.SELF), result);
    }

    @Test
    void 权限集合为仅本人_只能分配仅本人() {
        assertEquals(List.of(DataScopeEnum.SELF),
                RoleService.getAssignableDataScopes(DataScopeEnum.SELF));
    }
}
