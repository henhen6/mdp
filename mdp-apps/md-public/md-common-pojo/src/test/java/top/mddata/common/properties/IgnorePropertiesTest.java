package top.mddata.common.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 白名单匹配行为守护。
 *
 * <p>baseUri 默认名单含多个 {@code **} 段的 pattern（如 {@code /**\/api-docs/**}），
 * AntPathMatcher 兼容此类写法，而 Spring PathPatternParser 不支持
 * （"Multiple {*...} or ** pattern elements are not allowed"）。
 * 本测试守护 isIgnore/isIgnoreUser 始终使用 AntPathMatcher 通道。</p>
 *
 * @author henhen6
 * @since 2026-10-05
 */
class IgnorePropertiesTest {

    private final IgnoreProperties props = new IgnoreProperties();

    @Test
    void 默认白名单_多段通配pattern正常匹配() {
        assertTrue(props.isIgnoreUser("GET", "/api/console/v3/api-docs/swagger-config"));
        assertTrue(props.isIgnoreUser("GET", "/api/workbench/webjars/locator/aa.js"));
        assertTrue(props.isIgnoreUser("GET", "/api/open/static/a.png"));
        assertTrue(props.isIgnoreUser("POST", "/api/console/anno/x"));
        assertTrue(props.isIgnoreUser("GET", "/actuator/health"));
        // ALL 方法名单：任意方法均放行
        assertTrue(props.isIgnoreUser("DELETE", "/api/console/druid/index.html"));
    }

    @Test
    void 默认白名单_业务接口不误放行() {
        assertFalse(props.isIgnoreUser("GET", "/api/console/organization/user/page"));
        assertFalse(props.isIgnoreUser("POST", "/api/workbench/organization/user/page"));
    }

    @Test
    void 幂等令牌接口_登录后免URI权限但不免登录() {
        assertTrue(props.isIgnoreUriAuth("GET", "/anyone/idempotent/token"));
        assertTrue(props.isIgnoreUriAuth("GET", "/console/anyone/idempotent/token"));
        assertTrue(props.isIgnoreUriAuth("GET", "/api/console/anyone/idempotent/token"));
        assertFalse(props.isIgnoreUser("GET", "/api/console/anyone/idempotent/token"));
        assertFalse(props.isIgnoreUriAuth("POST", "/api/console/anyone/idempotent/token"));
    }
}
