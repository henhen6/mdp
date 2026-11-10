package top.mddata.gateway.inner.apiperm;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.mddata.base.apiperm.spi.ApiPermProvider;
import top.mddata.common.properties.IgnoreProperties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/** cloud 网关严格 URI 权限下的令牌签发契约。 */
class GatewayApiPermSupportTest {

    @Test
    void nonAdminWithLoginCanIssueUnderStrictUriPermission() {
        ApiPermProvider provider = mock(ApiPermProvider.class);
        when(provider.isAuthEnabled()).thenReturn(true);
        when(provider.isNotConfigAllow()).thenReturn(false);
        GatewayApiPermSupport support = new GatewayApiPermSupport(provider, new IgnoreProperties());
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdDefaultNull).thenReturn(42L);

            assertTrue(support.pass("/api/console/anyone/idempotent/token", "GET"));
        }
    }
}
