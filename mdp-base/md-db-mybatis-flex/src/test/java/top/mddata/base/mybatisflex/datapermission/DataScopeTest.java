package top.mddata.base.mybatisflex.datapermission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 数据范围枚举基线测试：编码是跨环境契约（mdc_role.data_scope 存储值）。
 */
class DataScopeTest {

    @Test
    void 五档加自定义编码稳定() {
        assertEquals("10", DataScope.ALL.getCode());
        assertEquals("20", DataScope.COMPANY_AND_CHILD.getCode());
        assertEquals("30", DataScope.DEPT_AND_CHILD.getCode());
        assertEquals("40", DataScope.DEPT.getCode());
        assertEquals("50", DataScope.SELF.getCode());
        assertEquals("90", DataScope.CUSTOM.getCode());
    }

    @Test
    void getByCode空值与无匹配返回null() {
        assertNull(DataScope.getByCode(null));
        assertNull(DataScope.getByCode("99"));
        assertEquals(DataScope.ALL, DataScope.getByCode("10"));
    }
}
