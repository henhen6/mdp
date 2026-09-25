package top.mddata.console.service.organization.accountop;

import org.junit.jupiter.api.Test;
import top.mddata.common.constant.BuiltInUserId;
import top.mddata.console.service.organization.accountop.strategy.OpsAdminStrategy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ops_admin 策略测试：仅 ops_admin 本人适用，对任意非自身、非 ops_admin 目标放行。
 */
class OpsAdminStrategyTest {
    private final OpsAdminStrategy strategy = new OpsAdminStrategy();

    @Test
    void 仅支持opsAdmin本人() {
        assertTrue(strategy.supports(BuiltInUserId.OPS_ADMIN));
        assertFalse(strategy.supports(BuiltInUserId.OPEN_ADMIN));
        assertFalse(strategy.supports(123456L));
        assertFalse(strategy.supports(null));
    }

    @Test
    void 对任意目标均放行() {
        TargetAccountProfile target = TargetAccountProfile.builder()
                .userId(999L)
                .operationsAdmin(true)
                .inDeveloperTree(true)
                .inHeadCompanyTree(true)
                .build();
        assertDoesNotThrow(() -> strategy.check(AccountOperation.DISABLE, target));
        assertDoesNotThrow(() -> strategy.check(AccountOperation.ENABLE, target));
        assertDoesNotThrow(() -> strategy.check(AccountOperation.RESET_PASSWORD, target));
    }
}
