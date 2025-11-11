package top.mddata.common.cache.console.organization;


import top.mddata.common.cache.CacheKeyModular;
import top.mddata.common.cache.CacheKeyTable;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;

import java.time.Duration;

/**
 * 组织
 *
 * @author henhen6
 * @date 2020/9/20 6:45 下午
 */
public class UserCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(Long id) {
        return new UserCacheKeyBuilder().key(id);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Center.SSO_USER;
    }

    @Override
    public String getPrefix() {
        return CacheKeyModular.PREFIX;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }
}
