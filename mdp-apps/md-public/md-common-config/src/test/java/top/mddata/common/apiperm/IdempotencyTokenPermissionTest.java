package top.mddata.common.apiperm;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.mddata.base.apiperm.spi.ApiPermProvider;
import top.mddata.base.exception.BizException;
import top.mddata.base.idempotent.controller.IdempotencyTokenController;
import top.mddata.base.idempotent.repository.IdempotencyRepository;
import top.mddata.base.idempotent.service.IdempotencyTokenService;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.properties.IgnoreProperties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/** boot 严格 URI 权限与匿名签发的接口契约。 */
class IdempotencyTokenPermissionTest {

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    void nonAdminWithLoginCanIssueUnderStrictUriPermission() {
        ApiPermProvider provider = mock(ApiPermProvider.class);
        when(provider.isAuthEnabled()).thenReturn(true);
        when(provider.isNotConfigAllow()).thenReturn(false);
        ApiPermSupport support = new ApiPermSupport(provider, new IgnoreProperties());
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(42L);

            assertDoesNotThrow(() -> support.check("/console/anyone/idempotent/token", "GET"));
        }
    }

    @Test
    void anonymousStillRejectedWithInvalidIdentityCode() {
        IdempotencyTokenService service = new IdempotencyTokenService(mock(IdempotencyRepository.class));
        IdempotencyTokenController controller = new IdempotencyTokenController(service);
        ContextUtil.remove();

        BizException exception = assertThrows(BizException.class, controller::token);

        assertEquals(-16, exception.getCode());
    }
}
