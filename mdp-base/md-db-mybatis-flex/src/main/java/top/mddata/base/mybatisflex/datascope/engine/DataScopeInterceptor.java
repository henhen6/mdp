package top.mddata.base.mybatisflex.datascope.engine;

import top.mddata.base.mybatisflex.datascope.annotation.DataScope;
import top.mddata.base.mybatisflex.datascope.context.DataScopeContext;
import top.mddata.base.mybatisflex.datascope.model.DataScopeCurrentUser;
import top.mddata.base.mybatisflex.datascope.model.DataScopeEnum;
import top.mddata.base.mybatisflex.datascope.model.DataScopeGrant;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeCustomHandler;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeProvider;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 数据权限拦截器：拦截 Executor#query，按（用户角色 × 菜单）授权
 * 用 JSqlParser 改写 SQL 注入数据范围条件。
 *
 * <p>覆盖全部查询路径（BaseMapper 方法 / QueryWrapper / XML 自定义 SQL /
 * Db 工具），这是选择 Executor 层而非 MyBatis-Flex 方言的原因：
 * 方言挂钩点够不到 XML 自定义 SQL。</p>
 *
 * @author henhen
 * @since 2026年09月26日
 */
@Slf4j
@RequiredArgsConstructor
@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class})})
public class DataScopeInterceptor implements Interceptor {

    private final DataScopeProvider dataScopeProvider;
    private final Map<String, DataScopeCustomHandler> customHandlers;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        BoundSql boundSql = args.length == 6 ? (BoundSql) args[5] : ms.getBoundSql(parameter);

        String newSql = process(boundSql.getSql());
        if (newSql == null) {
            return invocation.proceed();
        }
        BoundSql newBoundSql = copyBoundSql(ms.getConfiguration(), boundSql, newSql);
        if (args.length == 6) {
            CacheKey cacheKey = (CacheKey) args[4];
            cacheKey.update(newSql);
            args[5] = newBoundSql;
        } else {
            args[0] = copyMappedStatement(ms, newBoundSql);
        }
        return invocation.proceed();
    }

    /**
     * 判定并改写 SQL（与 MyBatis 解耦，供单测）
     *
     * @return 改写后的 SQL；放行（无需过滤）时返回 null
     */
    String process(String sql) {
        DataScope dataScope = DataScopeContext.get();
        if (dataScope == null || !dataScopeProvider.isFilter()) {
            return null;
        }
        Long menuId;
        DataScopeCurrentUser currentUser;
        // 菜单与授权查询是引擎内部查询，会再次经过本拦截器，
        // 必须隔离上下文，否则无限自递归（StackOverflowError）
        DataScope previous = DataScopeContext.setAndGetPrevious(null);
        try {
            menuId = dataScopeProvider.findEnabledMenuId(dataScope.code());
            if (menuId == null) {
                return null;
            }
            currentUser = dataScopeProvider.getCurrentUser(menuId);
        } finally {
            DataScopeContext.restore(previous);
        }
        if (currentUser == null || currentUser.getUserId() == null) {
            // 系统线程（worker/MQ）无登录上下文，不过滤
            return null;
        }
        List<DataScopeGrant> effective = DataScopeMerger.selectEffective(currentUser.getGrants());
        if (CollUtil.isEmpty(effective)) {
            // 菜单已启用但所有角色均未授权：最小权限，无数据
            return DataScopeSqlRewriter.denyAll(sql);
        }
        boolean hasAll = effective.stream()
                .anyMatch(grant -> DataScopeEnum.ALL.equals(grant.getScope()));
        if (hasAll) {
            return null;
        }
        return DataScopeSqlRewriter.rewrite(sql, dataScope, effective, currentUser,
                customHandlers);
    }

    /**
     * 复制 BoundSql 并迁移附加参数（foreach 等动态参数挂在
     * additionalParameters 上，不复制会丢参数）
     */
    private BoundSql copyBoundSql(Configuration configuration, BoundSql boundSql, String newSql) {
        BoundSql newBoundSql = new BoundSql(configuration, newSql,
                boundSql.getParameterMappings(), boundSql.getParameterObject());
        MetaObject newMeta = configuration.newMetaObject(newBoundSql);
        MetaObject oldMeta = configuration.newMetaObject(boundSql);
        newMeta.setValue("additionalParameters", oldMeta.getValue("additionalParameters"));
        if (oldMeta.hasGetter("metaParameters")) {
            newMeta.setValue("metaParameters", oldMeta.getValue("metaParameters"));
        }
        return newBoundSql;
    }

    /**
     * 以固定 SqlSource 复制 MappedStatement（4 参 query 路径需要替换整个语句）
     */
    private MappedStatement copyMappedStatement(MappedStatement ms, BoundSql newBoundSql) {
        SqlSource sqlSource = parameterObject -> newBoundSql;
        return new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), sqlSource,
                ms.getSqlCommandType())
                .resource(ms.getResource())
                .fetchSize(ms.getFetchSize())
                .timeout(ms.getTimeout())
                .statementType(ms.getStatementType())
                .keyGenerator(ms.getKeyGenerator())
                .keyProperty(ms.getKeyProperties() == null ? null
                        : String.join(",", ms.getKeyProperties()))
                .keyColumn(ms.getKeyColumns() == null ? null
                        : String.join(",", ms.getKeyColumns()))
                .databaseId(ms.getDatabaseId())
                .lang(ms.getLang())
                .resultOrdered(ms.isResultOrdered())
                .resultSets(ms.getResultSets() == null ? null
                        : String.join(",", ms.getResultSets()))
                .resultMaps(ms.getResultMaps())
                .resultSetType(ms.getResultSetType())
                .flushCacheRequired(ms.isFlushCacheRequired())
                .useCache(ms.isUseCache())
                .cache(ms.getCache())
                .build();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 无配置项
    }
}
