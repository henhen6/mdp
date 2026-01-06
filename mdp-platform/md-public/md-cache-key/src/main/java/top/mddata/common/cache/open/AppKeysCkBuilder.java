package top.mddata.common.cache.open;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 应用秘钥
 * appId -> obj
 *
 * @author tangyh
 * @since 2026/1/6 16:55
 */
public class AppKeysCkBuilder implements CacheKeyBuilder {
    /**
     * 构造器
     * @param appId 应用ID
     * @return key
     */
    public static CacheKey builder(Long appId) {
        return new AppKeysCkBuilder().key(appId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Open.APP_KEYS;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofDays(1);
    }
}
