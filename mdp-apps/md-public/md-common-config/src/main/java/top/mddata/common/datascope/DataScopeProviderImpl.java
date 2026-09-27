package top.mddata.common.datascope;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mybatisflex.datascope.model.DataScopeCurrentUser;
import top.mddata.base.mybatisflex.datascope.model.DataScopeEnum;
import top.mddata.base.mybatisflex.datascope.model.DataScopeGrant;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeProvider;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.cache.console.permission.MenuDataScopeCacheKeyBuilder;
import top.mddata.common.cache.console.permission.RoleDataScopeCacheKeyBuilder;

import java.util.List;
import java.util.Objects;

/**
 * 数据权限数据提供方实现。
 *
 * <p>跨模块直查 mdc_ 表（MyBatis-Flex Row Db），避免 md-public 依赖 console 实体；
 * 菜单开关与角色授权走缓存，授权保存时由 RoleDataScopeRelServiceImpl
 * 负责失效。</p>
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Component
@RequiredArgsConstructor
public class DataScopeProviderImpl implements DataScopeProvider {
    private final CacheOps cacheOps;

    @Override
    public boolean isFilter() {
        return true;
    }

    @Override
    public Long findEnabledMenuId(String menuCode) {
        CacheKey key = MenuDataScopeCacheKeyBuilder.build(menuCode);
        CacheResult<Long> result = cacheOps.get(key, k -> {
            Row row = Db.selectOneByQuery(QueryWrapper.create()
                    .select("id")
                    .from("mdc_resource_menu")
                    .where("code = ?", menuCode)
                    .and("data_scope_state = ?", Boolean.TRUE)
                    .and("deleted_at = 0"));
            return row == null ? null : row.getLong("id");
        });
        return result.getValue();
    }

    @Override
    public DataScopeCurrentUser getCurrentUser(Long menuId) {
        DataScopeCurrentUser currentUser = new DataScopeCurrentUser();
        Long userId = ContextUtil.getUserId();
        currentUser.setUserId(userId);
        currentUser.setCompanyId(ContextUtil.getCurrentCompanyId());
        // 部门基准取组织单元（部门优先、公司回落，规则见 ContextUtil.getCurrentDeptOrCompanyId）
        currentUser.setDeptId(ContextUtil.getCurrentDeptOrCompanyId());
        if (userId == null) {
            return currentUser;
        }
        currentUser.setGrants(findGrants(userId, menuId));
        return currentUser;
    }

    /**
     * 查用户所有启用角色对目标菜单的授权（按角色逐个走缓存）
     */
    private List<DataScopeGrant> findGrants(Long userId, Long menuId) {
        List<Row> roleRows = Db.selectListByQuery(QueryWrapper.create()
                .select("r.id AS roleId")
                .from("mdc_user_role_rel").as("ur")
                .innerJoin("mdc_role").as("r").on("ur.role_id = r.id")
                .where("ur.user_id = ?", userId)
                .and("r.state = ?", Boolean.TRUE)
                .and("r.deleted_at = 0"));
        return roleRows.stream()
                .map(row -> findGrantOfRole(row.getLong("roleId"), menuId))
                .filter(Objects::nonNull)
                .toList();
    }

    private DataScopeGrant findGrantOfRole(Long roleId, Long menuId) {
        CacheKey key = RoleDataScopeCacheKeyBuilder.build(roleId, menuId);
        CacheResult<DataScopeGrant> result = cacheOps.get(key, k -> {
            Row row = Db.selectOneByQuery(QueryWrapper.create()
                    .select("data_scope AS dataScope", "data_scope_impl AS dataScopeImpl")
                    .from("mdc_role_data_scope_rel")
                    .where("role_id = ?", roleId)
                    .and("menu_id = ?", menuId));
            return row == null ? null : new DataScopeGrant(roleId,
                    DataScopeEnum.getByCode(row.getString("dataScope")),
                    row.getString("dataScopeImpl"));
        });
        return result.getValue();
    }
}
