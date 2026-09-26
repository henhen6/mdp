package top.mddata.base.mybatisflex.datascope;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 多角色数据范围合并规则测试：并集取最高优先级档
 * （全部 > 自定义 > 公司及以下 > 部门及以下 > 部门 > 仅本人）。
 */
class DataScopeMergerTest {

    private static DataScopeGrant grant(Long roleId, DataScopeEnum scope) {
        return new DataScopeGrant(roleId, scope, null);
    }

    @Test
    void 空集合返回空() {
        assertTrue(DataScopeMerger.selectEffective(null).isEmpty());
        assertTrue(DataScopeMerger.selectEffective(Set.of()).isEmpty());
    }

    @Test
    void 全部数据档直接胜出() {
        List<DataScopeGrant> result = DataScopeMerger.selectEffective(
                Set.of(grant(1L, DataScopeEnum.SELF), grant(2L, DataScopeEnum.ALL)));
        assertEquals(1, result.size());
        assertEquals(DataScopeEnum.ALL, result.get(0).getScope());
    }

    @Test
    void 自定义档优先于内置组织档() {
        List<DataScopeGrant> result = DataScopeMerger.selectEffective(
                Set.of(grant(1L, DataScopeEnum.COMPANY_AND_CHILD),
                        grant(2L, DataScopeEnum.CUSTOM)));
        assertEquals(1, result.size());
        assertEquals(DataScopeEnum.CUSTOM, result.get(0).getScope());
    }

    @Test
    void 内置档取最高优先级() {
        List<DataScopeGrant> result = DataScopeMerger.selectEffective(
                Set.of(grant(1L, DataScopeEnum.SELF), grant(2L, DataScopeEnum.DEPT),
                        grant(3L, DataScopeEnum.DEPT_AND_CHILD)));
        assertEquals(1, result.size());
        assertEquals(DataScopeEnum.DEPT_AND_CHILD, result.get(0).getScope());
    }

    @Test
    void 多个自定义档全部保留待OR合并() {
        List<DataScopeGrant> result = DataScopeMerger.selectEffective(
                Set.of(grant(1L, DataScopeEnum.CUSTOM), grant(2L, DataScopeEnum.CUSTOM),
                        grant(3L, DataScopeEnum.SELF)));
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(g -> DataScopeEnum.CUSTOM.equals(g.getScope())));
    }

    @Test
    void 授权档位为空的记录被忽略() {
        List<DataScopeGrant> result = DataScopeMerger.selectEffective(
                Set.of(new DataScopeGrant(1L, null, null), grant(2L, DataScopeEnum.SELF)));
        assertEquals(1, result.size());
        assertEquals(DataScopeEnum.SELF, result.get(0).getScope());
    }
}
