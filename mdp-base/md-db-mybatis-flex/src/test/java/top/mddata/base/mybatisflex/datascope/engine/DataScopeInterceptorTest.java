package top.mddata.base.mybatisflex.datascope.engine;

import top.mddata.base.mybatisflex.datascope.annotation.DataScope;
import top.mddata.base.mybatisflex.datascope.context.DataScopeContext;
import top.mddata.base.mybatisflex.datascope.model.DataScopeCurrentUser;
import top.mddata.base.mybatisflex.datascope.model.DataScopeEnum;
import top.mddata.base.mybatisflex.datascope.model.DataScopeGrant;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeCustomHandler;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void 无任何授权_注入永假条件() {
        DataScopeContext.setAndGetPrevious(stub("menu:user"));
        DataScopeCurrentUser user = userWithGrant(DataScopeEnum.SELF);
        user.setGrants(List.of());
        DataScopeInterceptor interceptor = interceptor(true, user);
        String sql = interceptor.process("SELECT id FROM mdc_user");
        // 永假条件用主键 IS NULL 表达（1 = 0 会被 Druid WallFilter 拦截）
        assertTrue(sql.contains("mdc_user.id IS NULL"), sql);
    }

    @Test
    void 仅本人档_改写() {
        DataScopeContext.setAndGetPrevious(stub("menu:user"));
        DataScopeInterceptor interceptor = interceptor(true, userWithGrant(DataScopeEnum.SELF));
        String sql = interceptor.process("SELECT id FROM mdc_user");
        assertTrue(sql.contains("created_by = 7"), sql);
    }

    /**
     * 回归：Provider 内部查询会再次经过拦截器，调用期间上下文必须被隔离，
     * 否则无限自递归（StackOverflowError）；返回后外层上下文必须恢复
     */
    @Test
    void provider调用期间上下文被隔离_返回后恢复() {
        DataScopeContext.setAndGetPrevious(stub("menu:user"));
        AtomicReference<DataScope> seenInFindMenu = new AtomicReference<>();
        AtomicReference<DataScope> seenInGetUser = new AtomicReference<>();
        DataScopeProvider provider = new DataScopeProvider() {
            @Override
            public boolean isFilter() {
                return true;
            }

            @Override
            public Long findEnabledMenuId(String menuCode) {
                seenInFindMenu.set(DataScopeContext.get());
                return 1L;
            }

            @Override
            public DataScopeCurrentUser getCurrentUser(Long menuId) {
                seenInGetUser.set(DataScopeContext.get());
                return userWithGrant(DataScopeEnum.SELF);
            }
        };
        String sql = new DataScopeInterceptor(provider, Map.of()).process("SELECT id FROM mdc_user");

        assertTrue(sql.contains("created_by = 7"), sql);
        assertNull(seenInFindMenu.get());
        assertNull(seenInGetUser.get());
        assertEquals("menu:user", DataScopeContext.get().code());
    }
}
