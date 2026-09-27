package top.mddata.base.apiperm.engine;

import org.junit.jupiter.api.Test;
import top.mddata.base.apiperm.model.ApiPattern;
import top.mddata.base.apiperm.model.UserApiPerm;
import top.mddata.base.apiperm.spi.ApiPermProvider;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApiPermCheckerTest {

    @Test
    void matches_精确匹配与method匹配() {
        ApiPattern p = new ApiPattern("/organization/user/page", "POST");
        assertTrue(p.matches("/organization/user/page", "POST"));
        assertFalse(p.matches("/organization/user/page", "GET"));
        assertFalse(p.matches("/organization/user/list", "POST"));
    }

    @Test
    void matches_ALL匹配任意method() {
        ApiPattern p = new ApiPattern("/organization/user/**", "ALL");
        assertTrue(p.matches("/organization/user/page", "GET"));
        assertTrue(p.matches("/organization/user/a/b", "DELETE"));
        assertFalse(p.matches("/organization/role/page", "GET"));
    }

    @Test
    void matches_中段通配与问号() {
        assertTrue(new ApiPattern("/user/*/detail", "GET").matches("/user/123/detail", "GET"));
        assertTrue(new ApiPattern("/user/?", "GET").matches("/user/1", "GET"));
        assertFalse(new ApiPattern("/user/?", "GET").matches("/user/12", "GET"));
    }

    @Test
    void normalizePath_剥网关前缀与服务前缀() {
        Set<String> prefixes = Set.of("console", "workbench", "open");
        assertEquals("/organization/user/page",
                ApiPermChecker.normalizePath("/api/console/organization/user/page", "api", prefixes));
        assertEquals("/organization/user/page",
                ApiPermChecker.normalizePath("/organization/user/page", "api", prefixes));
    }

    @Test
    void isManaged_任一命中即纳管() {
        List<ApiPattern> all = List.of(
                new ApiPattern("/organization/user/page", "POST"),
                new ApiPattern("/organization/user/**", "ALL"));
        assertTrue(ApiPermChecker.isManaged(all, "/organization/user/page", "POST"));
        assertFalse(ApiPermChecker.isManaged(all, "/organization/role/page", "POST"));
    }

    // ---------- check() 集成用例 ----------

    /**
     * authEnabled=false 直接放行，provider 其余方法不应被调用（短路验证）。
     */
    @Test
    void check_authDisabled直接放行且短路() {
        ApiPermProvider provider = new ApiPermProvider() {
            @Override
            public boolean isAuthEnabled() {
                return false;
            }

            @Override
            public boolean isNotConfigAllow() {
                throw new AssertionError("authDisabled 时不应调用 isNotConfigAllow");
            }

            @Override
            public String getGatewayPrefix() {
                throw new AssertionError("authDisabled 时不应调用 getGatewayPrefix");
            }

            @Override
            public Set<String> getServicePrefixes() {
                throw new AssertionError("authDisabled 时不应调用 getServicePrefixes");
            }

            @Override
            public List<ApiPattern> findAllPatterns() {
                throw new AssertionError("authDisabled 时不应调用 findAllPatterns");
            }

            @Override
            public UserApiPerm findUserPerm(Long userId) {
                throw new AssertionError("authDisabled 时不应调用 findUserPerm");
            }
        };
        assertTrue(ApiPermChecker.check(provider, "/any/path", "POST", 1L));
    }

    /**
     * 未纳管 + notConfigAllow=true → 放行。
     */
    @Test
    void check_未纳管且未配置放行_放行() {
        ApiPermProvider provider = stubProvider(true, true, List.of(),
                new UserApiPerm(false, Set.of()));
        assertTrue(ApiPermChecker.check(provider,
                "/api/console/organization/role/page", "POST", 1L));
    }

    /**
     * 未纳管 + notConfigAllow=false → 拒绝。
     */
    @Test
    void check_未纳管且未配置拒绝_拒绝() {
        ApiPermProvider provider = stubProvider(true, false, List.of(),
                new UserApiPerm(false, Set.of()));
        assertFalse(ApiPermChecker.check(provider,
                "/api/console/organization/role/page", "POST", 1L));
    }

    /**
     * 纳管 + 运营者豁免 → 放行（即使 patterns 为空）。
     */
    @Test
    void check_纳管且运营者_豁免放行() {
        List<ApiPattern> all = List.of(
                new ApiPattern("/organization/user/page", "POST"));
        UserApiPerm perm = new UserApiPerm(true, Set.of());
        ApiPermProvider provider = stubProvider(true, false, all, perm);
        assertTrue(ApiPermChecker.check(provider,
                "/api/console/organization/user/page", "POST", 1L));
    }

    /**
     * 纳管 + 普通用户：放行集命中则放行，未命中则拒绝。
     */
    @Test
    void check_纳管普通用户_按放行集判定() {
        List<ApiPattern> all = List.of(
                new ApiPattern("/organization/user/page", "POST"),
                new ApiPattern("/organization/role/page", "POST"));
        UserApiPerm perm = new UserApiPerm(false, Set.of(
                new ApiPattern("/organization/user/**", "ALL")));
        ApiPermProvider provider = stubProvider(true, false, all, perm);

        // 命中放行集 → 放行
        assertTrue(ApiPermChecker.check(provider,
                "/api/console/organization/user/page", "POST", 1L));
        // 未命中 → 拒绝
        assertFalse(ApiPermChecker.check(provider,
                "/api/console/organization/role/page", "POST", 1L));
    }

    /**
     * 构造一个固定返回的 provider stub。
     */
    private ApiPermProvider stubProvider(boolean authEnabled, boolean notConfigAllow,
                                         List<ApiPattern> allPatterns, UserApiPerm userPerm) {
        return new ApiPermProvider() {
            @Override
            public boolean isAuthEnabled() {
                return authEnabled;
            }

            @Override
            public boolean isNotConfigAllow() {
                return notConfigAllow;
            }

            @Override
            public String getGatewayPrefix() {
                return "api";
            }

            @Override
            public Set<String> getServicePrefixes() {
                return Set.of("console", "workbench", "open");
            }

            @Override
            public List<ApiPattern> findAllPatterns() {
                return allPatterns;
            }

            @Override
            public UserApiPerm findUserPerm(Long userId) {
                return userPerm;
            }
        };
    }
}
