package top.mddata.console.service.organization;

import org.junit.jupiter.api.Test;
import top.mddata.common.constant.BuiltInOrgId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内置组织判定测试：3 根公司 + 默认部门受保护，其余组织不拦截。
 */
class BuiltInOrgCheckTest {

    @Test
    void 内置组织全部命中() {
        assertTrue(SystemProtectService.isBuiltInOrg(BuiltInOrgId.OPERATIONS_CENTER));
        assertTrue(SystemProtectService.isBuiltInOrg(BuiltInOrgId.DEVELOPER_PLATFORM));
        assertTrue(SystemProtectService.isBuiltInOrg(BuiltInOrgId.HEAD_COMPANY));
        assertTrue(SystemProtectService.isBuiltInOrg(BuiltInOrgId.DEFAULT_DEPT));
    }

    @Test
    void 普通组织与空值不命中() {
        assertFalse(SystemProtectService.isBuiltInOrg(123456L));
        assertFalse(SystemProtectService.isBuiltInOrg(null));
    }
}
