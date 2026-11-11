package top.mddata.common.cache.workbench;

import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 敏感操作二次验证票据缓存键
 *
 * <p>reauth 票据及一次性消费标记共用此前缀，有效期 5 分钟。</p>
 *
 * @author tangyh
 */
public class SocialReauthCacheKeyBuilder implements CacheKeyBuilder {

    @Override
    public String getTable() {
        return CacheKeyTable.Workbench.SOCIAL_REAUTH;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofMinutes(5);
    }
}
