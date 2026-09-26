package top.mddata.common.cache.console.permission;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 角色 × 菜单 数据范围授权 KEY
 *
 * @author henhen6
 * @since 2026-09-26
 */
public class RoleDataScopeCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(Long roleId, Long menuId) {
        return new RoleDataScopeCacheKeyBuilder().key(roleId, menuId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Console.ROLE_DATA_SCOPE;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }
}
