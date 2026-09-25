package top.mddata.common.enumeration.organization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 组织性质枚举基线测试：编码与名称是跨环境契约，改动必须显式感知。
 */
class OrgNatureEnumTest {

    @Test
    void 内置三个组织性质且编码稳定() {
        assertEquals(3, OrgNatureEnum.values().length);
        assertEquals(1, OrgNatureEnum.HEAD_COMPANY.getCode());
        assertEquals("总公司", OrgNatureEnum.HEAD_COMPANY.getDesc());
        assertEquals(90, OrgNatureEnum.DEVELOPER.getCode());
        assertEquals("开发者", OrgNatureEnum.DEVELOPER.getDesc());
        assertEquals(99, OrgNatureEnum.OPERATIONS.getCode());
        assertEquals("运营", OrgNatureEnum.OPERATIONS.getDesc());
    }
}
