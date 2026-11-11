package top.mddata.common.cache.workbench;

import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 第三方未绑定待选择票据缓存键
 *
 * <p>pending 票据、失败计数与一次性消费标记共用此前缀，有效期 10 分钟。</p>
 *
 * @author tangyh
 */
public class SocialPendingCacheKeyBuilder implements CacheKeyBuilder {

    @Override
    public String getTable() {
        return CacheKeyTable.Workbench.SOCIAL_PENDING;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofMinutes(10);
    }
}
