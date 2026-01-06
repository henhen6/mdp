package top.mddata.common.cache.open;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 开放接口
 * method + version -> id
 *
 * @author tangyh
 * @since 2026/1/6 16:55
 */
public class ApiByMethodVersionCkBuilder implements CacheKeyBuilder {
    /**
     * 构造器
     * @param method 方法
     * @param version 版本
     * @return key
     */
    public static CacheKey builder(String method, String version) {
        return new ApiByMethodVersionCkBuilder().key(method, version);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Open.API;
    }

    @Override
    public String getField() {
        return "method:version";
    }

    @Override
    public Duration getExpire() {
        return Duration.ofDays(1);
    }
}
