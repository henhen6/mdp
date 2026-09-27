package top.mddata.base.mybatisflex.datascope.spi;

import top.mddata.base.mybatisflex.datascope.annotation.DataScope;
import top.mddata.base.mybatisflex.datascope.model.DataScopeCurrentUser;

/**
 * 自定义数据范围处理器 SPI。
 *
 * <p>实现类注册为 Spring Bean，授权记录（data_scope_impl）配置 Bean 名。
 * 返回的是 SQL 条件片段（可引用别名参数），由引擎解析后以 AND 注入 WHERE。
 * 注意：单个实现返回空白会被忽略，但一条授权链上的实现全部返回空白时
 * 引擎按配置错误 fail fast 抛异常（不静默放行）。</p>
 *
 * @author henhen
 * @since 2026年09月26日
 */
public interface DataScopeCustomHandler {

    /**
     * 构建自定义数据范围条件
     *
     * @param currentUser 当前用户视图
     * @param dataScope   触发拦截的注解（含 orgColumn/userColumn 等声明）
     * @param tableAlias  命中的表别名；单表无别名时为 null
     * @return SQL 条件片段，如 "t.area_id in (1,2,3)"
     */
    String buildCondition(DataScopeCurrentUser currentUser, DataScope dataScope, String tableAlias);
}
