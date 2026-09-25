package top.mddata.console.service.organization.accountop;

import org.junit.jupiter.api.Test;
import top.mddata.base.exception.ArgumentException;
import top.mddata.common.constant.BuiltInUserId;
import top.mddata.console.service.organization.accountop.strategy.OpenAdminStrategy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * open_admin 策略测试：仅可管理开发者平台树内的账号。
 */
class OpenAdminStrategyTest {
    private final OpenAdminStrategy strategy = new OpenAdminStrategy();

    @Test
    void 仅支持openAdmin本人() {
        assertTrue(strategy.supports(BuiltInUserId.OPEN_ADMIN));
        assertFalse(strategy.supports(BuiltInUserId.OPS_ADMIN));
        assertFalse(strategy.supports(123456L));
        assertFalse(strategy.supports(null));
    }

    @Test
    void 目标在开发者平台树内则放行() {
        TargetAccountProfile target = TargetAccountProfile.builder()
                .userId(9L).inDeveloperTree(true).build();
        assertDoesNotThrow(() -> strategy.check(AccountOperation.DISABLE, target));
        assertDoesNotThrow(() -> strategy.check(AccountOperation.RESET_PASSWORD, target));
    }

    @Test
    void 目标不在开发者平台树内则拒绝() {
        TargetAccountProfile target = TargetAccountProfile.builder()
                .userId(9L).inDeveloperTree(false).inHeadCompanyTree(true).build();
        ArgumentException e = assertThrows(ArgumentException.class,
                () -> strategy.check(AccountOperation.DISABLE, target));
        assertTrue(e.getMessage().contains("open_admin 仅可管理开发者平台下的账号"));
    }
}
