package top.mddata.base.mybatisflex.datapermission;

import org.junit.jupiter.api.Test;
import top.mddata.base.mybatisflex.datapermission.DataPermissionCurrentUser.CurrentUserRole;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 多角色数据范围合并规则测试。
 */
class EffectiveRolesTest {

    private static CurrentUserRole role(Long id, DataScope scope) {
        return new CurrentUserRole(id, scope, null);
    }

    @Test
    void 空集合返回空() {
        assertTrue(DataPermissionDialect.selectEffectiveRoles(null).isEmpty());
        assertTrue(DataPermissionDialect.selectEffectiveRoles(Set.of()).isEmpty());
    }

    @Test
    void 内置档取范围最大档() {
        List<CurrentUserRole> result = DataPermissionDialect.selectEffectiveRoles(
                Set.of(role(1L, DataScope.SELF), role(2L, DataScope.DEPT_AND_CHILD)));
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getRoleId());
    }

    @Test
    void 内置档存在时自定义档被忽略() {
        List<CurrentUserRole> result = DataPermissionDialect.selectEffectiveRoles(
                Set.of(role(1L, DataScope.CUSTOM), role(2L, DataScope.SELF)));
        assertEquals(1, result.size());
        assertEquals(DataScope.SELF, result.get(0).getDataScope());
    }

    @Test
    void 全部为自定义档时全部保留() {
        List<CurrentUserRole> result = DataPermissionDialect.selectEffectiveRoles(
                Set.of(role(1L, DataScope.CUSTOM), role(2L, DataScope.CUSTOM)));
        assertEquals(2, result.size());
    }

    @Test
    void 全部数据档直接保留() {
        List<CurrentUserRole> result = DataPermissionDialect.selectEffectiveRoles(
                Set.of(role(1L, DataScope.SELF), role(2L, DataScope.ALL)));
        assertEquals(1, result.size());
        assertEquals(DataScope.ALL, result.get(0).getDataScope());
    }
}
