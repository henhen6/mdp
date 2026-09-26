package top.mddata.base.mybatisflex.datascope;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.utils.ArgumentAssert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据权限 SQL 改写器（纯函数，JSqlParser）。
 *
 * <p>把数据范围条件以 AND 注入 WHERE（而非 JOIN ON）：
 * 数据权限语义是"结果集行过滤"，可空侧不满足范围的行不应返回。</p>
 *
 * @author henhen
 * @since 2026年09月26日
 */
public final class DataScopeSqlRewriter {

    private DataScopeSqlRewriter() {
    }

    /**
     * 改写 SELECT 语句，注入数据范围条件
     *
     * @return 改写后的 SQL；语句不涉及声明别名（辅助查询）时返回 null
     */
    public static String rewrite(String sql, DataScope annotation,
                                 List<DataScopeGrant> effectiveGrants,
                                 DataScopeCurrentUser currentUser,
                                 Map<String, DataScopeCustomHandler> customHandlers) {
        PlainSelect plainSelect = parsePlainSelect(sql);
        List<String> matchedAliases = matchAliases(plainSelect, annotation);
        if (matchedAliases == null) {
            return null;
        }
        ArgumentAssert.notEmpty(effectiveGrants,
                "数据权限：有效授权为空时不应进入改写，code={}", annotation.code());
        String condition = buildCondition(annotation, effectiveGrants, currentUser,
                customHandlers, matchedAliases);
        return inject(plainSelect, condition);
    }

    /**
     * 注入 1 = 0（菜单已启用但无任何授权：无数据）
     */
    public static String denyAll(String sql) {
        PlainSelect plainSelect = parsePlainSelect(sql);
        return inject(plainSelect, "1 = 0");
    }

    private static PlainSelect parsePlainSelect(String sql) {
        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            throw new ArgumentException(
                    "数据权限：SQL 解析失败，请调整该查询语句：" + sql, e);
        }
        ArgumentAssert.isTrue(statement instanceof Select,
                "数据权限：仅支持 SELECT 语句，sql={}", sql);
        ArgumentAssert.isTrue(statement instanceof PlainSelect,
                "数据权限：不支持 UNION 等复合查询，sql={}", sql);
        return (PlainSelect) statement;
    }

    /**
     * 匹配声明别名
     *
     * @return 命中的别名列表（元素可为 null，表示单表无别名用裸列名）；
     *         返回 null 表示该语句不涉及声明别名
     */
    private static List<String> matchAliases(PlainSelect plainSelect, DataScope annotation) {
        String[] declared = annotation.tableAliases();
        if (declared.length == 0) {
            List<Join> joins = plainSelect.getJoins();
            ArgumentAssert.isTrue(CollUtil.isEmpty(joins),
                    "数据权限：多表语句必须在注解上声明 tableAliases，code={}",
                    annotation.code());
            ArgumentAssert.notNull(plainSelect.getFromItem(),
                    "数据权限：缺少 FROM 表，code={}", annotation.code());
            List<String> single = new ArrayList<>(1);
            single.add(null);
            return single;
        }
        Set<String> presentAliases = collectAliases(plainSelect);
        List<String> matched = Arrays.stream(declared)
                .filter(presentAliases::contains)
                .toList();
        return matched.isEmpty() ? null : matched;
    }

    private static Set<String> collectAliases(PlainSelect plainSelect) {
        Set<String> aliases = new HashSet<>();
        addFromItem(aliases, plainSelect.getFromItem());
        if (plainSelect.getJoins() != null) {
            plainSelect.getJoins().forEach(join -> addFromItem(aliases, join.getRightItem()));
        }
        return aliases;
    }

    private static void addFromItem(Set<String> aliases, FromItem item) {
        if (item instanceof net.sf.jsqlparser.schema.Table table) {
            aliases.add(table.getAlias() != null ? table.getAlias().getName() : table.getName());
        }
    }

    /**
     * 构建完整过滤条件：多个命中别名之间 AND；多个自定义实现之间 OR
     */
    private static String buildCondition(DataScope annotation, List<DataScopeGrant> effectiveGrants,
                                         DataScopeCurrentUser currentUser,
                                         Map<String, DataScopeCustomHandler> customHandlers,
                                         List<String> matchedAliases) {
        List<String> perAlias = new ArrayList<>();
        for (String alias : matchedAliases) {
            perAlias.add(buildSingleAliasCondition(annotation, effectiveGrants,
                    currentUser, customHandlers, alias));
        }
        return StrUtil.join(" AND ", perAlias);
    }

    private static String buildSingleAliasCondition(DataScope annotation,
            List<DataScopeGrant> effectiveGrants,
            DataScopeCurrentUser currentUser,
            Map<String, DataScopeCustomHandler> customHandlers,
            String alias) {
        DataScopeEnum scope = effectiveGrants.get(0).getScope();
        if (!DataScopeEnum.CUSTOM.equals(scope)) {
            return buildBuiltInCondition(scope, annotation, currentUser, alias);
        }
        // 多个自定义实现：条件 OR 合并（并集语义）
        List<String> conditions = new ArrayList<>();
        for (DataScopeGrant grant : effectiveGrants) {
            DataScopeCustomHandler handler = customHandlers.get(grant.getScopeImpl());
            ArgumentAssert.notNull(handler,
                    "数据权限：角色[{}]配置的自定义实现[{}]不存在",
                    grant.getRoleId(), grant.getScopeImpl());
            String condition = handler.buildCondition(currentUser, annotation, alias);
            if (StrUtil.isNotBlank(condition)) {
                conditions.add("(" + condition + ")");
            }
        }
        // 全部 handler 返回空属于配置错误，拒绝放行
        ArgumentAssert.notEmpty(conditions,
                "数据权限：自定义实现均未返回有效条件，roleId={}",
                effectiveGrants.get(0).getRoleId());
        return conditions.size() == 1 ? conditions.get(0)
                : "(" + StrUtil.join(" OR ", conditions) + ")";
    }

    private static String buildBuiltInCondition(DataScopeEnum scope, DataScope annotation,
                                                DataScopeCurrentUser currentUser, String alias) {
        return switch (scope) {
            case COMPANY_AND_CHILD -> orgTreeCondition(annotation,
                    currentUser.getCompanyId(), alias);
            case DEPT_AND_CHILD -> orgTreeCondition(annotation, currentUser.getDeptId(), alias);
            case DEPT -> eqCondition(column(alias, orgColumn(annotation)), currentUser.getDeptId());
            case SELF -> eqCondition(column(alias, annotation.userColumn()),
                    currentUser.getUserId());
            default -> throw new ArgumentException(
                    "数据权限：不支持的内置档位：" + scope);
        };
    }

    private static String orgColumn(DataScope annotation) {
        ArgumentAssert.isTrue(StrUtil.isNotBlank(annotation.orgColumn()),
                "数据权限：组织类档位要求注解声明 orgColumn，code={}",
                annotation.code());
        return annotation.orgColumn();
    }

    /**
     * 组织子树：mdc_org.tree_path 前缀匹配（id 为 Long，拼接无注入风险）
     */
    private static String orgTreeCondition(DataScope annotation, Long rootOrgId, String alias) {
        if (rootOrgId == null) {
            return "1 = 0";
        }
        return "%s IN (SELECT id FROM mdc_org WHERE tree_path LIKE '%%/%d/%%')"
                .formatted(column(alias, orgColumn(annotation)), rootOrgId);
    }

    private static String eqCondition(String column, Long value) {
        if (value == null) {
            return "1 = 0";
        }
        return "%s = %d".formatted(column, value);
    }

    private static String column(String alias, String columnName) {
        return alias == null ? columnName : alias + "." + columnName;
    }

    private static String inject(PlainSelect plainSelect, String condition) {
        try {
            Expression expression = CCJSqlParserUtil.parseCondExpression(condition);
            Expression where = plainSelect.getWhere();
            plainSelect.setWhere(where == null ? expression : new AndExpression(where, expression));
            return plainSelect.toString();
        } catch (JSQLParserException e) {
            throw new ArgumentException(
                    "数据权限：过滤条件解析失败：" + condition, e);
        }
    }
}
