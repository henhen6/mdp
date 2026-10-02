package top.mddata.base.mvcflex.advice;

import org.junit.jupiter.api.Test;
import top.mddata.base.apiperm.model.ApiPattern;
import top.mddata.base.apiperm.model.UserApiPerm;
import top.mddata.base.apiperm.spi.ApiPermProvider;
import top.mddata.base.fieldperm.model.FieldRule;
import top.mddata.base.fieldperm.model.UserFieldPerm;
import top.mddata.base.fieldperm.spi.FieldPermProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 字段权限响应决策链：开关 → 登录态 → URI 反查菜单 → 用户受限规则。
 *
 * @author henhen6
 * @since 2026-10-02
 */
class FieldPermAdviceTest {

    private final StubFieldPermProvider fieldPerm = new StubFieldPermProvider();
    private final ApiPermProvider apiPerm = new StubApiPermProvider();

    @Test
    void 开关关闭直接放行() {
        fieldPerm.authEnabled = false;
        assertNull(FieldPermAdvice.resolveRules(fieldPerm, apiPerm,
                "/api/console/organization/user/page", "POST", 1L));
    }

    @Test
    void 未登录直接放行() {
        assertNull(FieldPermAdvice.resolveRules(fieldPerm, apiPerm,
                "/api/console/organization/user/page", "POST", null));
    }

    @Test
    void 网关与服务前缀归一化后反查菜单() {
        fieldPerm.uriMenu.put("POST /organization/user/page", 100L);
        FieldPermAdvice.resolveRules(fieldPerm, apiPerm,
                "/api/console/organization/user/page", "POST", 1L);
        // 断言反查入参是归一化后的路径
        assertEquals("/organization/user/page", fieldPerm.lastUri);
    }

    @Test
    void 接口未绑定字段规则菜单时放行() {
        assertNull(FieldPermAdvice.resolveRules(fieldPerm, apiPerm,
                "/api/console/organization/role/page", "POST", 1L));
    }

    @Test
    void 命中菜单时返回当前用户受限规则() {
        fieldPerm.uriMenu.put("POST /organization/user/page", 100L);
        fieldPerm.userPerm = new UserFieldPerm(false, Map.of(
                100L, Map.of("phone", FieldRule.mask("mobile"))));

        Map<String, FieldRule> rules = FieldPermAdvice.resolveRules(fieldPerm, apiPerm,
                "/api/console/organization/user/page", "POST", 1L);

        assertEquals("mobile", rules.get("phone").getMaskRule());
    }

    @Test
    void 运营管理员豁免返回空规则() {
        fieldPerm.uriMenu.put("POST /organization/user/page", 100L);
        fieldPerm.userPerm = new UserFieldPerm(true, Map.of());

        assertNull(FieldPermAdvice.resolveRules(fieldPerm, apiPerm,
                "/api/console/organization/user/page", "POST", 1L));
    }

    /** 用户在该菜单无受限字段时放行 */
    @Test
    void 用户无该菜单受限字段时放行() {
        fieldPerm.uriMenu.put("POST /organization/user/page", 100L);
        fieldPerm.userPerm = new UserFieldPerm(false, Map.of(
                200L, Map.of("phone", FieldRule.hide())));

        assertNull(FieldPermAdvice.resolveRules(fieldPerm, apiPerm,
                "/api/console/organization/user/page", "POST", 1L));
    }

    static class StubFieldPermProvider implements FieldPermProvider {
        boolean authEnabled = true;
        Map<String, Long> uriMenu = new java.util.HashMap<>();
        UserFieldPerm userPerm = new UserFieldPerm(false, Map.of());
        String lastUri;

        @Override
        public boolean isAuthEnabled() {
            return authEnabled;
        }

        @Override
        public UserFieldPerm findUserPerm(Long userId) {
            return userPerm;
        }

        @Override
        public Long findMenuId(String uri, String method) {
            this.lastUri = uri;
            return uriMenu.get(method.toUpperCase() + " " + uri);
        }
    }

    static class StubApiPermProvider implements ApiPermProvider {
        @Override
        public boolean isAuthEnabled() {
            return true;
        }

        @Override
        public boolean isNotConfigAllow() {
            return true;
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
            return List.of();
        }

        @Override
        public UserApiPerm findUserPerm(Long userId) {
            return new UserApiPerm(false, Set.of());
        }
    }
}
