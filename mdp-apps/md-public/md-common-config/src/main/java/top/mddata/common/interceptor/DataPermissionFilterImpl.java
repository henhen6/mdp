package top.mddata.common.interceptor;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import org.springframework.stereotype.Component;
import top.mddata.base.mybatisflex.datapermission.DataPermissionCurrentUser;
import top.mddata.base.mybatisflex.datapermission.DataPermissionFilter;
import top.mddata.base.mybatisflex.datapermission.DataScope;
import top.mddata.base.util.ContextUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限数据实现类。
 *
 * <p>加载当前用户及其启用角色的数据范围（跨模块查询 mdc_role，
 * 用 MyBatis-Flex Row Db 避免依赖 console 实体）。</p>
 *
 * @author henhen
 * @since 2026/5/24 23:38
 */
@Component
public class DataPermissionFilterImpl implements DataPermissionFilter {
    @Override
    public boolean isFilter() {
        return true;
    }

    @Override
    public DataPermissionCurrentUser getCurrentUser() {
        DataPermissionCurrentUser currentUser = new DataPermissionCurrentUser();
        currentUser.setUserId(ContextUtil.getUserId());
        currentUser.setDeptId(ContextUtil.getCurrentDeptId());
        currentUser.setCompanyId(ContextUtil.getCurrentCompanyId());
        currentUser.setRoles(findRoles(ContextUtil.getUserId()));
        return currentUser;
    }

    /**
     * 查询用户绑定的启用角色及其数据范围配置
     */
    private Set<DataPermissionCurrentUser.CurrentUserRole> findRoles(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        QueryWrapper wrapper = QueryWrapper.create()
                .select("r.id AS roleId", "r.data_scope AS dataScope",
                        "r.data_scope_impl AS dataScopeImpl")
                .from("mdc_user_role_rel").as("ur")
                .innerJoin("mdc_role").as("r").on("ur.role_id = r.id")
                .where("ur.user_id = ?", userId)
                .and("r.state = ?", Boolean.TRUE)
                .and("r.deleted_at = 0");
        List<Row> rows = Db.selectListByQuery(wrapper);
        return rows.stream().map(row -> new DataPermissionCurrentUser.CurrentUserRole(
                row.getLong("roleId"),
                DataScope.getByCode(row.getString("dataScope")),
                row.getString("dataScopeImpl")))
                .collect(Collectors.toSet());
    }
}
