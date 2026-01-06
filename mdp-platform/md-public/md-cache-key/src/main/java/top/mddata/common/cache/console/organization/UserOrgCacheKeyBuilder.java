package top.mddata.common.cache.console.organization;


import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 用户拥有那些组织
 *
 * @author henhen6
 * @date 2020/9/20 6:45 下午
 */
public class UserOrgCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(Long userId) {
        return new UserOrgCacheKeyBuilder().key(userId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Console.USER_ORG;
    }


    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }
}
