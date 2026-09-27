package top.mddata.base.mybatisflex.datascope.engine;

import top.mddata.base.mybatisflex.datascope.annotation.DataScope;
import top.mddata.base.mybatisflex.datascope.context.DataScopeContext;
import top.mddata.base.mybatisflex.datascope.model.DataScopeCurrentUser;
import top.mddata.base.mybatisflex.datascope.model.DataScopeEnum;
import top.mddata.base.mybatisflex.datascope.model.DataScopeGrant;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeCustomHandler;

import org.junit.jupiter.api.Test;
import top.mddata.base.exception.ArgumentException;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SQL 改写器测试：单表、inner/left/right join、多别名、
 * 辅助语句跳过、fail fast 全路径。
 */
class DataScopeSqlRewriterTest {

    private static DataScope stub(String code, String orgColumn,
                                  String userColumn, String... aliases) {
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
                return orgColumn;
            }

            @Override
            public String userColumn() {
                return userColumn;
            }

            @Override
            public String[] tableAliases() {
                return aliases;
            }
        };
    }

    private static DataScopeCurrentUser user() {
        DataScopeCurrentUser user = new DataScopeCurrentUser();
        user.setUserId(7L);
        user.setCompanyId(100L);
        user.setDeptId(200L);
        return user;
    }

    private static DataScopeGrant grant(DataScopeEnum scope) {
        return new DataScopeGrant(1L, scope, null);
    }

    private static String rewrite(String sql, DataScope annotation, List<DataScopeGrant> grants) {
        return DataScopeSqlRewriter.rewrite(sql, annotation, grants, user(), Map.of());
    }

    @Test
    void 单表_本部门() {
        String sql = rewrite("SELECT id, dept_id FROM mdc_user WHERE state = 1",
                stub("menu:user", "dept_id", "created_by"), List.of(grant(DataScopeEnum.DEPT)));
        assertTrue(sql.contains("dept_id = 200"), sql);
        assertTrue(sql.contains("state = 1"), sql);
    }

    @Test
    void 单表_仅本人_自定义用户列() {
        String sql = rewrite("SELECT id FROM mdc_file",
                stub("menu:file", "dept_id", "owner_id"), List.of(grant(DataScopeEnum.SELF)));
        assertTrue(sql.contains("owner_id = 7"), sql);
    }

    @Test
    void 单表_本公司及以下_组织子树子查询() {
        String sql = rewrite("SELECT id FROM mdc_user",
                stub("menu:user", "dept_id", "created_by"),
                List.of(grant(DataScopeEnum.COMPANY_AND_CHILD)));
        assertTrue(sql.contains(
                "dept_id IN (SELECT id FROM mdc_org WHERE tree_path LIKE '%/100/%')"), sql);
    }

    @Test
    void 单表_本部门及以下() {
        String sql = rewrite("SELECT id FROM mdc_user",
                stub("menu:user", "dept_id", "created_by"),
                List.of(grant(DataScopeEnum.DEPT_AND_CHILD)));
        assertTrue(sql.contains("'%/200/%'"), sql);
    }

    @Test
    void 多表join_声明别名_逐别名注入() {
        DataScope annotation = stub("menu:user", "dept_id", "created_by", "u", "o");
        String template = "SELECT u.id FROM mdc_user u %s JOIN mdc_org o ON u.dept_id = o.id";
        for (String joinType : List.of("", "LEFT", "RIGHT")) {
            String sql = rewrite(template.formatted(joinType), annotation,
                    List.of(grant(DataScopeEnum.DEPT)));
            assertTrue(sql.contains("u.dept_id = 200"), sql);
            assertTrue(sql.contains("o.dept_id = 200"), sql);
        }
    }

    @Test
    void 多表join_仅命中部分声明别名() {
        String sql = rewrite(
                "SELECT u.id, d.name FROM mdc_user u LEFT JOIN mdc_dict d ON u.type = d.code",
                stub("menu:user", "dept_id", "created_by", "u", "o"),
                List.of(grant(DataScopeEnum.SELF)));
        assertTrue(sql.contains("u.created_by = 7"), sql);
        assertTrue(!sql.contains("d.created_by"), sql);
    }

    @Test
    void 辅助语句_不含声明别名_跳过() {
        String sql = rewrite("SELECT id, name FROM mdc_dict",
                stub("menu:user", "dept_id", "created_by", "u"),
                List.of(grant(DataScopeEnum.DEPT)));
        assertNull(sql);
    }

    @Test
    void 多表未声明别名_failFast() {
        DataScope annotation = stub("menu:user", "dept_id", "created_by");
        assertThrows(ArgumentException.class, () -> rewrite(
                "SELECT u.id FROM mdc_user u JOIN mdc_org o ON u.dept_id = o.id",
                annotation, List.of(grant(DataScopeEnum.DEPT))));
    }

    @Test
    void 组织类档位缺少orgColumn_failFast() {
        DataScope annotation = stub("menu:user", "", "created_by");
        assertThrows(ArgumentException.class, () -> rewrite("SELECT id FROM mdc_user",
                annotation, List.of(grant(DataScopeEnum.DEPT))));
    }

    @Test
    void 上下文部门为空_该档位无数据() {
        DataScopeCurrentUser noDept = user();
        noDept.setDeptId(null);
        String sql = DataScopeSqlRewriter.rewrite("SELECT id FROM mdc_user",
                stub("menu:user", "dept_id", "created_by"),
                List.of(grant(DataScopeEnum.DEPT)), noDept, Map.of());
        assertTrue(sql.contains("id IS NULL"), sql);
    }

    @Test
    void 自定义档_委托handler_多实现OR合并() {
        Map<String, DataScopeCustomHandler> handlers = Map.of(
                "handlerA", (user, ds, alias) -> "area_id IN (1, 2)",
                "handlerB", (user, ds, alias) -> "area_id IN (3)");
        List<DataScopeGrant> grants = List.of(
                new DataScopeGrant(1L, DataScopeEnum.CUSTOM, "handlerA"),
                new DataScopeGrant(2L, DataScopeEnum.CUSTOM, "handlerB"));
        String sql = DataScopeSqlRewriter.rewrite("SELECT id FROM mdc_order",
                stub("menu:order", "dept_id", "created_by"), grants, user(), handlers);
        assertTrue(sql.contains("area_id IN (1, 2)"), sql);
        assertTrue(sql.contains("area_id IN (3)"), sql);
        assertTrue(sql.toUpperCase().contains(" OR "), sql);
    }

    @Test
    void 自定义档Bean不存在_failFast() {
        List<DataScopeGrant> grants =
                List.of(new DataScopeGrant(1L, DataScopeEnum.CUSTOM, "missing"));
        DataScope annotation = stub("menu:order", "dept_id", "created_by");
        assertThrows(ArgumentException.class, () -> DataScopeSqlRewriter.rewrite(
                "SELECT id FROM mdc_order", annotation, grants, user(), Map.of()));
    }

    @Test
    void denyAll注入永假条件() {
        String sql = DataScopeSqlRewriter.denyAll("SELECT id FROM mdc_user WHERE state = 1");
        // 永假条件用主键 IS NULL 表达（1 = 0 会被 Druid WallFilter 拦截）
        assertTrue(sql.contains("mdc_user.id IS NULL"), sql);
        assertTrue(sql.contains("state = 1"), sql);
    }

    @Test
    void denyAll带别名按别名拼永假条件() {
        String sql = DataScopeSqlRewriter.denyAll("SELECT u.id FROM mdc_user u WHERE u.state = 1");
        assertTrue(sql.contains("u.id IS NULL"), sql);
    }

    @Test
    void 分页count语句同样被改写() {
        String sql = rewrite("SELECT COUNT(*) FROM mdc_user",
                stub("menu:user", "dept_id", "created_by"), List.of(grant(DataScopeEnum.DEPT)));
        assertTrue(sql.contains("dept_id = 200"), sql);
    }

    @Test
    void union复合语句_failFast() {
        DataScope annotation = stub("menu:user", "dept_id", "created_by");
        assertThrows(ArgumentException.class, () -> rewrite(
                "SELECT id FROM mdc_user UNION SELECT id FROM mdc_admin",
                annotation, List.of(grant(DataScopeEnum.DEPT))));
    }

    @Test
    void sql解析失败_failFast() {
        DataScope annotation = stub("menu:user", "dept_id", "created_by");
        assertThrows(ArgumentException.class, () -> rewrite(
                "NOT A VALID SQL ###",
                annotation, List.of(grant(DataScopeEnum.DEPT))));
    }

    @Test
    void 全部handler返回空白_failFast() {
        Map<String, DataScopeCustomHandler> handlers = Map.of(
                "handlerA", (user, ds, alias) -> "",
                "handlerB", (user, ds, alias) -> "   ");
        List<DataScopeGrant> grants = List.of(
                new DataScopeGrant(1L, DataScopeEnum.CUSTOM, "handlerA"),
                new DataScopeGrant(2L, DataScopeEnum.CUSTOM, "handlerB"));
        DataScope annotation = stub("menu:order", "dept_id", "created_by");
        assertThrows(ArgumentException.class, () -> DataScopeSqlRewriter.rewrite(
                "SELECT id FROM mdc_order", annotation, grants, user(), handlers));
    }

    @Test
    void 上下文公司为空_本公司及以下档无数据() {
        DataScopeCurrentUser noCompany = user();
        noCompany.setCompanyId(null);
        String sql = DataScopeSqlRewriter.rewrite("SELECT id FROM mdc_user",
                stub("menu:user", "dept_id", "created_by"),
                List.of(grant(DataScopeEnum.COMPANY_AND_CHILD)), noCompany, Map.of());
        assertTrue(sql.contains("id IS NULL"), sql);
    }

    /**
     * 回归：自定义 handler 内部若查库会再次经过拦截器，执行期间上下文必须被隔离；
     * 返回后外层上下文必须恢复
     */
    @Test
    void 自定义handler执行期间上下文被隔离_返回后恢复() {
        AtomicReference<DataScope> seenInHandler = new AtomicReference<>();
        DataScopeCustomHandler handler = (currentUser, ds, alias) -> {
            seenInHandler.set(DataScopeContext.get());
            return "area_id = 1";
        };
        DataScope annotation = stub("menu:order", "dept_id", "created_by");
        DataScopeContext.setAndGetPrevious(annotation);
        try {
            String sql = DataScopeSqlRewriter.rewrite("SELECT id FROM mdc_order",
                    annotation, List.of(new DataScopeGrant(1L, DataScopeEnum.CUSTOM, "h")),
                    user(), Map.of("h", handler));

            assertTrue(sql.contains("area_id = 1"), sql);
            assertNull(seenInHandler.get());
            assertEquals("menu:order", DataScopeContext.get().code());
        } finally {
            DataScopeContext.restore(null);
        }
    }
}
