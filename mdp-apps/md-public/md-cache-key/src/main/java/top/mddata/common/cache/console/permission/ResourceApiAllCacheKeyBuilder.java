package top.mddata.common.cache.console.permission;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 全量已配置接口 KEY
 *
 * @author henhen6
 */
public class ResourceApiAllCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build() {
        return new ResourceApiAllCacheKeyBuilder().key();
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Console.RESOURCE_API_ALL;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }

}
