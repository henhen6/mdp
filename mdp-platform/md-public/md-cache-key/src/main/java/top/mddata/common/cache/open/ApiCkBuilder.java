package top.mddata.common.cache.open;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 开放接口
 * id -> obj
 *
 * @author henhen
 * @since 2026/1/6 16:55
 */
public class ApiCkBuilder implements CacheKeyBuilder {
    /**
     * 构造器
     * @param id 参数id
     * @return key
     */
    public static CacheKey builder(Long id) {
        return new ApiCkBuilder().key(id);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Open.API;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofDays(1);
    }
}
