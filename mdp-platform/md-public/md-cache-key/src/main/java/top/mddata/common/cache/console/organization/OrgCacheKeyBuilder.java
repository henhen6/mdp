package top.mddata.common.cache.console.organization;


import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyModular;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 组织
 *
 * @author henhen6
 * @date 2020/9/20 6:45 下午
 */
public class OrgCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(Long id) {
        return new OrgCacheKeyBuilder().key(id);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Admin.SYS_ORG;
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
