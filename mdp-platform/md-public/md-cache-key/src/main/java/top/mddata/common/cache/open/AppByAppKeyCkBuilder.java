package top.mddata.common.cache.open;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 开放应用
 * appKey -> id
 *
 * @author henhen
 * @since 2026/1/6 16:55
 */
public class AppByAppKeyCkBuilder implements CacheKeyBuilder {
    /**
     * 构造器
     *
     * @param appKey 应用Key
     * @return key
     */
    public static CacheKey builder(String appKey) {
        return new AppByAppKeyCkBuilder().key(appKey);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Open.APP;
    }

    @Override
    public String getField() {
        return "key";
    }

    @Override
    public Duration getExpire() {
        return Duration.ofDays(1);
    }
}
