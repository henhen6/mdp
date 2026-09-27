package top.mddata.common.cache.console.permission;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 用户接口放行集 KEY
 *
 * @author henhen6
 */
public class UserResourceApiCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(Long userId) {
        return new UserResourceApiCacheKeyBuilder().key(userId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Console.USER_RESOURCE_API;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }

}
