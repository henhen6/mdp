package top.mddata.console.mapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.junit.jupiter.api.Test;
import top.mddata.common.mapper.OrgMapper;
import top.mddata.common.mapper.UserMapper;
import top.mddata.console.mapper.permission.RoleMapper;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 动态 SQL 离线校验：@Select 注解内容经 MyBatis XMLLanguageDriver 解析，
 * 验证"参数为 null 时不拼接过滤子句"的 &lt;if&gt; 动态 SQL 行为，无需数据库。
 */
class DynamicSqlNullSwitchTest {

    private static final XMLLanguageDriver DRIVER = new XMLLanguageDriver();
    private static final Configuration CONFIG = new Configuration();

    /**
     * 读取 Mapper 方法的 @Select 内容，按给定参数生成最终 SQL
     */
    private String sqlOf(Class<?> mapperClass, String methodName, Class<?>[] paramTypes,
                         Map<String, Object> params) throws Exception {
        Method method = mapperClass.getMethod(methodName, paramTypes);
        String script = String.join("", method.getAnnotation(Select.class).value());
        SqlSource sqlSource = DRIVER.createSqlSource(CONFIG, script, Map.class);
        BoundSql boundSql = sqlSource.getBoundSql(params);
        return boundSql.getSql().replaceAll("\\s+", " ");
    }

    @Test
    void countUsersInScope_null前缀不含EXISTS() throws Exception {
        String sql = sqlOf(UserMapper.class, "countUsersInScope",
                new Class[]{String.class}, new HashMap<>());

        assertFalse(sql.contains("EXISTS"), "null 前缀不应拼接 EXISTS 子句: " + sql);
        assertFalse(sql.contains("tree_path"), "null 前缀不应出现 tree_path 过滤: " + sql);
    }

    @Test
    void countUsersInScope_非null前缀含EXISTS() throws Exception {
        Map<String, Object> params = Map.of("treePathPrefix", "/100/%");
        String sql = sqlOf(UserMapper.class, "countUsersInScope",
                new Class[]{String.class}, params);

        assertTrue(sql.contains("EXISTS"), "非 null 前缀应拼接 EXISTS 子句: " + sql);
        assertTrue(sql.contains("tree_path LIKE ?"), "非 null 前缀应含 tree_path 过滤: " + sql);
    }

    @Test
    void countByState_全null参数不加任何动态条件() throws Exception {
        String sql = sqlOf(UserMapper.class, "countByState",
                new Class[]{LocalDateTime.class, LocalDateTime.class, String.class}, new HashMap<>());

        assertFalse(sql.contains("created_at >="), "null startTime 不应拼接起始时间: " + sql);
        assertFalse(sql.contains("created_at <="), "null endTime 不应拼接截止时间: " + sql);
        assertFalse(sql.contains("EXISTS"), "null 前缀不应拼接 EXISTS: " + sql);
    }

    @Test
    void countByState_全参数拼接全部动态条件() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", LocalDateTime.now());
        params.put("endTime", LocalDateTime.now());
        params.put("treePathPrefix", "/100/%");
        String sql = sqlOf(UserMapper.class, "countByState",
                new Class[]{LocalDateTime.class, LocalDateTime.class, String.class}, params);

        assertTrue(sql.contains("created_at >="), "应拼接起始时间: " + sql);
        assertTrue(sql.contains("created_at <="), "应拼接截止时间: " + sql);
        assertTrue(sql.contains("EXISTS"), "应拼接 EXISTS: " + sql);
    }

    @Test
    void countByDayRange_可解析且动态拼接() throws Exception {
        // 全 null：无时间条件、无 EXISTS
        String allNullSql = sqlOf(UserMapper.class, "countByDayRange",
                new Class[]{LocalDateTime.class, LocalDateTime.class, String.class}, new HashMap<>());
        assertFalse(allNullSql.contains("created_at >="), "null startTime 不应拼接: " + allNullSql);
        assertFalse(allNullSql.contains("created_at <="), "null endTime 不应拼接: " + allNullSql);
        assertFalse(allNullSql.contains("EXISTS"), "null 前缀不应拼接 EXISTS: " + allNullSql);

        // 有日期无前缀：有时间条件（&lt;= 转义后应还原为 <=）、无 EXISTS
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", LocalDateTime.now());
        params.put("endTime", LocalDateTime.now());
        String allSql = sqlOf(UserMapper.class, "countByDayRange",
                new Class[]{LocalDateTime.class, LocalDateTime.class, String.class}, params);
        assertFalse(allSql.contains("EXISTS"), "null 前缀不应拼接 EXISTS: " + allSql);
        assertTrue(allSql.contains("created_at <="), "截止时间条件应保留（&lt;= 转义后应还原为 <=）: " + allSql);

        // 全参数：全部拼接
        params.put("treePathPrefix", "/100/%");
        String scopedSql = sqlOf(UserMapper.class, "countByDayRange",
                new Class[]{LocalDateTime.class, LocalDateTime.class, String.class}, params);
        assertTrue(scopedSql.contains("EXISTS"), "非 null 前缀应拼接 EXISTS: " + scopedSql);
    }

    @Test
    void countBySex_可解析且动态拼接() throws Exception {
        // 全 null：无时间条件、无 EXISTS
        String allNullSql = sqlOf(UserMapper.class, "countBySex",
                new Class[]{LocalDateTime.class, LocalDateTime.class, String.class}, new HashMap<>());
        assertFalse(allNullSql.contains("created_at >="), "null startTime 不应拼接: " + allNullSql);
        assertFalse(allNullSql.contains("created_at <="), "null endTime 不应拼接: " + allNullSql);
        assertFalse(allNullSql.contains("EXISTS"), "null 前缀不应拼接 EXISTS: " + allNullSql);

        Map<String, Object> params = new HashMap<>();
        params.put("startTime", LocalDateTime.now());
        params.put("endTime", LocalDateTime.now());
        String allSql = sqlOf(UserMapper.class, "countBySex",
                new Class[]{LocalDateTime.class, LocalDateTime.class, String.class}, params);
        assertFalse(allSql.contains("EXISTS"), "null 前缀不应拼接 EXISTS: " + allSql);

        params.put("treePathPrefix", "/100/%");
        String scopedSql = sqlOf(UserMapper.class, "countBySex",
                new Class[]{LocalDateTime.class, LocalDateTime.class, String.class}, params);
        assertTrue(scopedSql.contains("EXISTS"), "非 null 前缀应拼接 EXISTS: " + scopedSql);
    }

    @Test
    void countByNature_时间条件动态拼接() throws Exception {
        String allSql = sqlOf(UserMapper.class, "countByNature",
                new Class[]{LocalDateTime.class, LocalDateTime.class}, new HashMap<>());
        assertFalse(allSql.contains("created_at >="), "null startTime 不应拼接: " + allSql);
        assertFalse(allSql.contains("created_at <="), "null endTime 不应拼接: " + allSql);
        assertTrue(allSql.contains("COUNT(DISTINCT u.id)"), "同一性质内应按用户去重: " + allSql);

        Map<String, Object> params = new HashMap<>();
        params.put("startTime", LocalDateTime.now());
        params.put("endTime", LocalDateTime.now());
        String rangedSql = sqlOf(UserMapper.class, "countByNature",
                new Class[]{LocalDateTime.class, LocalDateTime.class}, params);
        assertTrue(rangedSql.contains("created_at >="), "应拼接起始时间: " + rangedSql);
        assertTrue(rangedSql.contains("created_at <="), "应拼接截止时间: " + rangedSql);
    }

    @Test
    void countByDayRangeGroupByNature_时间条件动态拼接() throws Exception {
        String allNullSql = sqlOf(UserMapper.class, "countByDayRangeGroupByNature",
                new Class[]{LocalDateTime.class, LocalDateTime.class}, new HashMap<>());
        assertFalse(allNullSql.contains("created_at >="), "null startTime 不应拼接: " + allNullSql);
        assertFalse(allNullSql.contains("created_at <="), "null endTime 不应拼接: " + allNullSql);
        assertTrue(allNullSql.contains("COUNT(DISTINCT u.id)"), "同一 (date, nature) 分组内应按用户去重: " + allNullSql);

        Map<String, Object> params = new HashMap<>();
        params.put("startTime", LocalDateTime.now());
        params.put("endTime", LocalDateTime.now());
        String rangedSql = sqlOf(UserMapper.class, "countByDayRangeGroupByNature",
                new Class[]{LocalDateTime.class, LocalDateTime.class}, params);
        assertTrue(rangedSql.contains("created_at >="), "应拼接起始时间: " + rangedSql);
        assertTrue(rangedSql.contains("created_at <="), "应拼接截止时间: " + rangedSql);
    }

    @Test
    void countNewUsersInMonth_时间条件动态拼接() throws Exception {
        String allNullSql = sqlOf(UserMapper.class, "countNewUsersInMonth",
                new Class[]{LocalDateTime.class, LocalDateTime.class, String.class}, new HashMap<>());
        assertFalse(allNullSql.contains("created_at >="), "null startTime 不应拼接: " + allNullSql);
        assertFalse(allNullSql.contains("created_at <="), "null endTime 不应拼接: " + allNullSql);

        Map<String, Object> params = new HashMap<>();
        params.put("startTime", LocalDateTime.now());
        params.put("endTime", LocalDateTime.now());
        String rangedSql = sqlOf(UserMapper.class, "countNewUsersInMonth",
                new Class[]{LocalDateTime.class, LocalDateTime.class, String.class}, params);
        assertTrue(rangedSql.contains("created_at >="), "应拼接起始时间: " + rangedSql);
        assertTrue(rangedSql.contains("created_at <="), "应拼接截止时间: " + rangedSql);
    }

    @Test
    void orgRank_null前缀不过滤treePath() throws Exception {
        String allSql = sqlOf(OrgMapper.class, "rankByUserCount",
                new Class[]{String.class, int.class}, new HashMap<>());
        assertFalse(allSql.contains("tree_path LIKE"), "null 前缀不应过滤 tree_path: " + allSql);

        String scopedSql = sqlOf(OrgMapper.class, "rankByUserCount",
                new Class[]{String.class, int.class}, Map.of("treePathPrefix", "/100/%"));
        assertTrue(scopedSql.contains("tree_path LIKE ?"), "非 null 前缀应过滤 tree_path: " + scopedSql);
    }

    @Test
    void roleRank_null性质不过滤orgNature() throws Exception {
        String allSql = sqlOf(RoleMapper.class, "rankByUserCount",
                new Class[]{Integer.class, int.class}, new HashMap<>());
        assertFalse(allSql.contains("org_nature ="), "null 性质不应过滤 org_nature: " + allSql);

        String scopedSql = sqlOf(RoleMapper.class, "rankByUserCount",
                new Class[]{Integer.class, int.class}, Map.of("orgNature", 1));
        assertTrue(scopedSql.contains("org_nature = ?"), "非 null 性质应过滤 org_nature: " + scopedSql);
    }
}
