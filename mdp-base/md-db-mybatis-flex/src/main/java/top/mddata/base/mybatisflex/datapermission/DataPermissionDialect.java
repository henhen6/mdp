package top.mddata.base.mybatisflex.datapermission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.mybatisflex.core.dialect.impl.CommonsDialectImpl;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import top.mddata.base.utils.ArgumentAssert;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 数据权限处理器实现类
 *
 * @author <a href="https://mybatis-flex.com/zh/core/data-permission.html">数据权限</a>
 * @author henhen
 * @since 2026年05月24日
 */
@Slf4j
public class DataPermissionDialect extends CommonsDialectImpl {

    private final DataPermissionFilter dataPermissionFilter;

    /**
     * 自定义数据范围处理器（key 为 Spring Bean 名，对应角色的 data_scope_impl）
     */
    private final Map<String, DataScopeCustomHandler> customHandlers;

    public DataPermissionDialect(DataPermissionFilter dataPermissionFilter,
                                 Map<String, DataScopeCustomHandler> customHandlers) {
        this.dataPermissionFilter = dataPermissionFilter;
        this.customHandlers = Objects.requireNonNull(customHandlers, "customHandlers 不能为 null");
    }

    @Override
    public String forSelectByQuery(QueryWrapper queryWrapper) {
        if (!dataPermissionFilter.isFilter()) {
            return super.buildSelectSql(queryWrapper);
        }
        DataPermission dataPermission = DataPermissionAspect.currentDataPermission();
        if (dataPermission == null) {
            return super.buildSelectSql(queryWrapper);
        }
        DataPermissionCurrentUser currentUser = dataPermissionFilter.getCurrentUser();
        List<DataPermissionCurrentUser.CurrentUserRole> effectiveRoles =
                selectEffectiveRoles(currentUser.getRoles());
        if (CollUtil.isEmpty(effectiveRoles)) {
            // 已认证零角色用户默认无数据可见（最小权限原则）；
            // 无登录上下文（worker/系统线程）放行，避免破坏系统内部调用
            if (currentUser.getUserId() != null) {
                log.debug("已认证零角色用户无数据可见，userId={}", currentUser.getUserId());
                queryWrapper.where("1 = 0");
            }
            return super.buildSelectSql(queryWrapper);
        }
        // 任一角色为全部数据，直接放行
        boolean hasAll = effectiveRoles.stream()
                .anyMatch(role -> DataScope.ALL.equals(role.getDataScope()));
        if (hasAll) {
            return super.buildSelectSql(queryWrapper);
        }
        // 多个生效角色的条件以 OR 合并（全部为 CUSTOM 角色的场景）
        if (effectiveRoles.size() == 1) {
            applyScope(dataPermission, currentUser, effectiveRoles.get(0), queryWrapper);
        } else {
            // 显式声明 Consumer 以消除与 and/or(LambdaGetter) 单参重载的歧义
            queryWrapper.and((Consumer<QueryWrapper>) qw ->
                    effectiveRoles.forEach(role ->
                            qw.or((Consumer<QueryWrapper>) orWrapper ->
                                    applyScope(dataPermission, currentUser, role, orWrapper))));
        }
        return super.buildSelectSql(queryWrapper);
    }

    /**
     * 多角色范围合并（纯函数）：
     * 存在内置档角色时，只保留范围最大档的角色
     * （全部 > 公司及以下 > 部门及以下 > 部门 > 仅本人）；
     * 全部为 CUSTOM 角色时全部保留（调用方以 OR 合并各 handler 条件）。
     *
     * @param roles 当前用户的角色集合
     * @return 生效角色列表，空入参返回空列表
     */
    public static List<DataPermissionCurrentUser.CurrentUserRole> selectEffectiveRoles(
            Collection<DataPermissionCurrentUser.CurrentUserRole> roles) {
        if (CollUtil.isEmpty(roles)) {
            return List.of();
        }
        List<DataScope> builtInScopes = roles.stream()
                .map(DataPermissionCurrentUser.CurrentUserRole::getDataScope)
                .filter(scope -> scope != null && !DataScope.CUSTOM.equals(scope))
                .toList();
        if (builtInScopes.isEmpty()) {
            return List.copyOf(roles);
        }
        DataScope maxScope = Collections.max(builtInScopes,
                Comparator.comparingInt(DataPermissionDialect::scopeRank));
        return roles.stream().filter(role -> maxScope.equals(role.getDataScope())).toList();
    }

    private static int scopeRank(DataScope scope) {
        return switch (scope) {
            case ALL -> 5;
            case COMPANY_AND_CHILD -> 4;
            case DEPT_AND_CHILD -> 3;
            case DEPT -> 2;
            case SELF -> 1;
            case CUSTOM -> 0;
        };
    }

    private void applyScope(DataPermission dataPermission,
                            DataPermissionCurrentUser currentUser,
                            DataPermissionCurrentUser.CurrentUserRole role,
                            QueryWrapper queryWrapper) {
        ArgumentAssert.notNull(role.getDataScope(),
                "角色[{}]的数据范围配置无效", role.getRoleId());
        switch (role.getDataScope()) {
            case COMPANY_AND_CHILD ->
                    buildOrgTreeExpression(dataPermission, currentUser.getCompanyId(),
                            queryWrapper);
            case DEPT_AND_CHILD ->
                    buildOrgTreeExpression(dataPermission, currentUser.getDeptId(),
                            queryWrapper);
            case DEPT -> queryWrapper.eq(
                    buildColumn(dataPermission.tableAlias(), dataPermission.deptId()),
                    currentUser.getDeptId());
            case SELF -> queryWrapper.eq(
                    buildColumn(dataPermission.tableAlias(), dataPermission.userId()),
                    currentUser.getUserId());
            case CUSTOM -> buildCustomExpression(dataPermission, currentUser, role, queryWrapper);
            default -> throw new IllegalArgumentException(
                    "暂不支持 [%s] 数据权限".formatted(role.getDataScope()));
        }
    }

    /**
     * 构建组织树范围表达式：目标表的部门列在指定组织子树内
     * （mdc_org.tree_path 前缀匹配）
     */
    private void buildOrgTreeExpression(DataPermission dataPermission, Long rootOrgId,
                                        QueryWrapper queryWrapper) {
        QueryWrapper subQueryWrapper = QueryWrapper.create();
        subQueryWrapper.select(dataPermission.id()).from(dataPermission.deptTableAlias());
        subQueryWrapper.like("tree_path", "/" + rootOrgId + "/");
        queryWrapper.in(buildColumn(dataPermission.tableAlias(), dataPermission.deptId()),
                subQueryWrapper);
    }

    /**
     * 构建自定义数据范围表达式：按角色配置的 Bean 名找到 SPI 实现并委托
     */
    private void buildCustomExpression(DataPermission dataPermission,
                                       DataPermissionCurrentUser currentUser,
                                       DataPermissionCurrentUser.CurrentUserRole role,
                                       QueryWrapper queryWrapper) {
        DataScopeCustomHandler handler = customHandlers.get(role.getDataScopeImpl());
        ArgumentAssert.notNull(handler,
                "角色[{}]配置的自定义数据范围实现[{}]不存在",
                role.getRoleId(), role.getDataScopeImpl());
        handler.apply(dataPermission, currentUser, queryWrapper);
    }

    /**
     * 构建 Column
     *
     * @param tableAlias 表别名
     * @param columnName 字段名称
     * @return 带表别名字段
     */
    private String buildColumn(String tableAlias, String columnName) {
        if (CharSequenceUtil.isNotEmpty(tableAlias)) {
            return "%s.%s".formatted(tableAlias, columnName);
        }
        return columnName;
    }

}
