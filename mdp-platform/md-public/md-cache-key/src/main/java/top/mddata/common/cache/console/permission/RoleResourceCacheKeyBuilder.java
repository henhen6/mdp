package top.mddata.common.cache.console.permission;


import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 角色的资源 KEY
 * <p>
 *
 * @author henhen6
 * @date 2020/9/20 6:45 下午
 */
public class RoleResourceCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(Long roleId, Long appId) {
        return new RoleResourceCacheKeyBuilder().key(roleId, appId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Console.ROLE_RESOURCE;
    }


    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }

}
