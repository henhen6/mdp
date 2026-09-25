package top.mddata.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内置数据常量基线测试：常量值必须与 docs/mdp.sql 中的内置数据一致。
 */
class BuiltInDataConstantTest {

    @Test
    void 内置组织ID与建库脚本一致() {
        assertEquals(687106879444787201L, BuiltInOrgId.OPERATIONS_CENTER);
        assertEquals(687106879444787202L, BuiltInOrgId.DEVELOPER_PLATFORM);
        assertEquals(687106879444787203L, BuiltInOrgId.HEAD_COMPANY);
        assertEquals(687106879444787204L, BuiltInOrgId.DEFAULT_DEPT);
    }

    @Test
    void 内置角色编码与建库脚本一致() {
        assertEquals("OPERATIONS_ADMIN", RoleCode.OPERATIONS_ADMIN);
        assertEquals("OPERATIONS_ADMIN_COLL", RoleCode.OPERATIONS_ADMIN_COLL);
        assertEquals("ADMIN", RoleCode.ADMIN);
        assertEquals("ADMIN_COLL", RoleCode.ADMIN_COLL);
        assertEquals("DEVELOPER_ADMIN", RoleCode.DEVELOPER_ADMIN);
        assertEquals("DEVELOPER_ADMIN_COLL", RoleCode.DEVELOPER_ADMIN_COLL);
        assertEquals("DEFAULT_DEVELOPER", RoleCode.DEFAULT_DEVELOPER);
        assertEquals("DEFAULT_USER", RoleCode.DEFAULT_USER);
    }

    @Test
    void 内置保留编码集合包含全部8个内置角色() {
        assertEquals(8, RoleCode.BUILT_IN_CODES.size());
        assertTrue(RoleCode.BUILT_IN_CODES.contains(RoleCode.OPERATIONS_ADMIN));
        assertTrue(RoleCode.BUILT_IN_CODES.contains(RoleCode.DEFAULT_USER));
    }
}
