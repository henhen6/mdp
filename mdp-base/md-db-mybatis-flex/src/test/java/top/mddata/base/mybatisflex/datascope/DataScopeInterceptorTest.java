package top.mddata.base.mybatisflex.datascope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 拦截器判定流程测试：放行路径、无数据注入、改写路径。
 * （process 方法与 MyBatis 解耦，可纯单测）
 */
class DataScopeInterceptorTest {

    @AfterEach
    void tearDown() {
        DataScopeContext.restore(null);
    }

    private static DataScope stub(String code, String... aliases) {
        return new DataScope() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DataScope.class;
            }

            @Override
            public String code() {
                return code;
            }

            @Override
            public String orgColumn() {
                return "dept_id";
            }

            @Override
            public String userColumn() {
                return "created_by";
            }

            @Override
            public String[] tableAliases() {
                return aliases;
            }
        };
    }

    /** 构造测试用拦截器：菜单开关与用户授权均由入参决定 */
    private static DataScopeInterceptor interceptor(
            boolean menuEnabled, DataScopeCurrentUser user) {
        DataScopeProvider provider = new DataScopeProvider() {
            @Override
            public boolean isFilter() {
                return true;
            }

            @Override
            public Long findEnabledMenuId(String menuCode) {
                return menuEnabled ? 1L : null;
            }

            @Override
            public DataScopeCurrentUser getCurrentUser(Long menuId) {
                return user;
            }
        };
        return new DataScopeInterceptor(provider, Map.of());
    }

    private static DataScopeCurrentUser userWithGrant(DataScopeEnum scope) {
        DataScopeCurrentUser user = new DataScopeCurrentUser();
        user.setUserId(7L);
        user.setDeptId(200L);
        user.setCompanyId(100L);
        user.setGrants(List.of(new DataScopeGrant(1L, scope, null)));
        return user;
    }

    @Test
    void 无注解_放行() {
        DataScopeInterceptor interceptor = interceptor(true, userWithGrant(DataScopeEnum.SELF));
        assertNull(interceptor.process("SELECT id FROM mdc_user"));
    }

    @Test
    void 菜单未启用_放行() {
        DataScopeContext.setAndGetPrevious(stub("menu:user"));
        DataScopeInterceptor interceptor = interceptor(false, userWithGrant(DataScopeEnum.SELF));
        assertNull(interceptor.process("SELECT id FROM mdc_user"));
    }

    @Test
    void 系统线程无登录上下文_放行() {
        DataScopeContext.setAndGetPrevious(stub("menu:user"));
        DataScopeInterceptor interceptor = interceptor(true, new DataScopeCurrentUser());
        assertNull(interceptor.process("SELECT id FROM mdc_user"));
    }

    @Test
    void 全部数据档_放行() {
        DataScopeContext.setAndGetPrevious(stub("menu:user"));
        DataScopeInterceptor interceptor = interceptor(true, userWithGrant(DataScopeEnum.ALL));
        assertNull(interceptor.process("SELECT id FROM mdc_user"));
    }

    @Test
    void 无任何授权_注入无数据() {
        DataScopeContext.setAndGetPrevious(stub("menu:user"));
        DataScopeCurrentUser user = userWithGrant(DataScopeEnum.SELF);
        user.setGrants(List.of());
        DataScopeInterceptor interceptor = interceptor(true, user);
        String sql = interceptor.process("SELECT id FROM mdc_user");
        assertTrue(sql.contains("1 = 0"), sql);
    }

    @Test
    void 仅本人档_改写() {
        DataScopeContext.setAndGetPrevious(stub("menu:user"));
        DataScopeInterceptor interceptor = interceptor(true, userWithGrant(DataScopeEnum.SELF));
        String sql = interceptor.process("SELECT id FROM mdc_user");
        assertTrue(sql.contains("created_by = 7"), sql);
    }
}
