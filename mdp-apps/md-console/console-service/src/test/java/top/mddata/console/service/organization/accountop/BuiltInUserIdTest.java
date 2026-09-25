package top.mddata.console.service.organization.accountop;

import org.junit.jupiter.api.Test;
import top.mddata.common.constant.BuiltInUserId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内置用户ID契约测试：取值必须与 docs/mdp.sql 中的内置数据一致。
 */
class BuiltInUserIdTest {

    @Test
    void 内置用户id与mdpSql契约一致() {
        assertEquals(680083598598475778L, BuiltInUserId.OPS_ADMIN);
        assertEquals(680083598598475779L, BuiltInUserId.OPEN_ADMIN);
        assertEquals(680083598598475780L, BuiltInUserId.ADMIN);
    }

    @Test
    void ALL包含全部内置用户() {
        assertEquals(3, BuiltInUserId.ALL.size());
        assertTrue(BuiltInUserId.ALL.contains(BuiltInUserId.OPS_ADMIN));
        assertTrue(BuiltInUserId.ALL.contains(BuiltInUserId.OPEN_ADMIN));
        assertTrue(BuiltInUserId.ALL.contains(BuiltInUserId.ADMIN));
    }
}
