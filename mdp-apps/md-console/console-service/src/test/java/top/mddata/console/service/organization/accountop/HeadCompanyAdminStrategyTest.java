package top.mddata.console.service.organization.accountop;

import org.junit.jupiter.api.Test;
import top.mddata.base.exception.ArgumentException;
import top.mddata.common.constant.BuiltInUserId;
import top.mddata.console.service.organization.accountop.strategy.HeadCompanyAdminStrategy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * admin 策略测试：仅可管理总公司树内的账号。
 */
class HeadCompanyAdminStrategyTest {
    private final HeadCompanyAdminStrategy strategy = new HeadCompanyAdminStrategy();

    @Test
    void 仅支持admin本人() {
        assertTrue(strategy.supports(BuiltInUserId.ADMIN));
        assertFalse(strategy.supports(BuiltInUserId.OPS_ADMIN));
        assertFalse(strategy.supports(123456L));
        assertFalse(strategy.supports(null));
    }

    @Test
    void 目标在总公司树内则放行() {
        TargetAccountProfile target = TargetAccountProfile.builder()
                .userId(9L).inHeadCompanyTree(true).build();
        assertDoesNotThrow(() -> strategy.check(AccountOperation.DISABLE, target));
        assertDoesNotThrow(() -> strategy.check(AccountOperation.RESET_PASSWORD, target));
    }

    @Test
    void 目标不在总公司树内则拒绝() {
        TargetAccountProfile target = TargetAccountProfile.builder()
                .userId(9L).inHeadCompanyTree(false).inDeveloperTree(true).build();
        ArgumentException e = assertThrows(ArgumentException.class,
                () -> strategy.check(AccountOperation.ENABLE, target));
        assertTrue(e.getMessage().contains("admin 仅可管理总公司下的账号"));
    }
}
