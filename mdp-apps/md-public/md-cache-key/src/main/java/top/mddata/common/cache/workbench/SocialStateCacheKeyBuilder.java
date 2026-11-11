package top.mddata.common.cache.workbench;

import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 第三方 OAuth state 缓存键
 *
 * <p>state 票据及一次性消费标记共用此前缀，有效期 10 分钟。</p>
 *
 * @author tangyh
 */
public class SocialStateCacheKeyBuilder implements CacheKeyBuilder {

    @Override
    public String getTable() {
        return CacheKeyTable.Workbench.SOCIAL_STATE;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofMinutes(10);
    }
}
