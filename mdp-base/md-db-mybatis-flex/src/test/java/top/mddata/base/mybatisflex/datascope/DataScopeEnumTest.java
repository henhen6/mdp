package top.mddata.base.mybatisflex.datascope;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 数据范围枚举基线测试：编码是跨环境契约
 * （mdc_role_data_scope_rel.data_scope 存储值）。
 */
class DataScopeEnumTest {

    @Test
    void 五档加自定义编码稳定() {
        assertEquals("10", DataScopeEnum.ALL.getCode());
        assertEquals("20", DataScopeEnum.COMPANY_AND_CHILD.getCode());
        assertEquals("30", DataScopeEnum.DEPT_AND_CHILD.getCode());
        assertEquals("40", DataScopeEnum.DEPT.getCode());
        assertEquals("50", DataScopeEnum.SELF.getCode());
        assertEquals("90", DataScopeEnum.CUSTOM.getCode());
    }

    @Test
    void 优先级契约_全部大于自定义大于组织档() {
        assertTrue(DataScopeEnum.ALL.priority() > DataScopeEnum.CUSTOM.priority());
        assertTrue(DataScopeEnum.CUSTOM.priority() > DataScopeEnum.COMPANY_AND_CHILD.priority());
        assertTrue(DataScopeEnum.COMPANY_AND_CHILD.priority()
                > DataScopeEnum.DEPT_AND_CHILD.priority());
        assertTrue(DataScopeEnum.DEPT_AND_CHILD.priority() > DataScopeEnum.DEPT.priority());
        assertTrue(DataScopeEnum.DEPT.priority() > DataScopeEnum.SELF.priority());
    }

    @Test
    void getByCode空值与无匹配返回null() {
        assertNull(DataScopeEnum.getByCode(null));
        assertNull(DataScopeEnum.getByCode("99"));
        assertEquals(DataScopeEnum.ALL, DataScopeEnum.getByCode("10"));
    }
}
