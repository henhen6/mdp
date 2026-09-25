package top.mddata.console.service.organization.accountop;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import top.mddata.base.exception.ArgumentException;
import top.mddata.console.service.organization.SystemProtectService;
import top.mddata.console.service.organization.accountop.strategy.OperationsAdminStrategy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 运营者策略测试：可管理非运营者目标；运营者目标仅 ops_admin 可管理（平级互不可管）。
 */
class OperationsAdminStrategyTest {
    private AutoCloseable mocks;
    @Mock
    private SystemProtectService systemProtectService;
    private OperationsAdminStrategy strategy;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        strategy = new OperationsAdminStrategy(systemProtectService);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void 仅运营者身份适用() {
        when(systemProtectService.isOperationsAdminUser(1L)).thenReturn(true);
        when(systemProtectService.isOperationsAdminUser(2L)).thenReturn(false);
        assertTrue(strategy.supports(1L));
        assertFalse(strategy.supports(2L));
    }

    @Test
    void 目标是运营者则拒绝() {
        TargetAccountProfile target = TargetAccountProfile.builder()
                .userId(9L).operationsAdmin(true).build();
        ArgumentException e = assertThrows(ArgumentException.class,
                () -> strategy.check(AccountOperation.RESET_PASSWORD, target));
        assertTrue(e.getMessage().contains("运营者账号仅 ops_admin 可管理"));
    }

    @Test
    void 目标非运营者则放行() {
        TargetAccountProfile target = TargetAccountProfile.builder()
                .userId(9L).operationsAdmin(false).build();
        assertDoesNotThrow(() -> strategy.check(AccountOperation.DISABLE, target));
        assertDoesNotThrow(() -> strategy.check(AccountOperation.ENABLE, target));
        assertDoesNotThrow(() -> strategy.check(AccountOperation.RESET_PASSWORD, target));
    }
}
