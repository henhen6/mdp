package top.mddata.base.mybatisflex.datapermission;

import com.mybatisflex.core.query.QueryWrapper;

/**
 * 自定义数据范围处理器（SPI 扩展点）。
 *
 * <p>五档内置数据范围无法满足需求时，开发人员实现本接口并注册为
 * Spring Bean，在角色的 data_scope_impl 字段配置 Bean 名
 * （data_scope=90 时生效）。
 * 实现类的职责：把该角色可见的数据范围条件追加到 queryWrapper。
 */
public interface DataScopeCustomHandler {

    /**
     * 追加自定义数据范围条件
     *
     * @param dataPermission 数据权限注解（含表别名、部门列、用户列等配置）
     * @param currentUser    当前用户上下文（userId、companyId、deptId）
     * @param queryWrapper   查询条件（在其上追加过滤）
     */
    void apply(DataPermission dataPermission, DataPermissionCurrentUser currentUser,
               QueryWrapper queryWrapper);
}
