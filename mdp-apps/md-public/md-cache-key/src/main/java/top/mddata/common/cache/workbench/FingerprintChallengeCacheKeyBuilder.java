package top.mddata.common.cache.workbench;

import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 指纹登录挑战 KEY（一次性消费，5 分钟过期）
 *
 * @author tangyh
 * @date 2026-11-13
 */
public class FingerprintChallengeCacheKeyBuilder implements CacheKeyBuilder {

    @Override
    public String getTable() {
        return CacheKeyTable.Workbench.FINGERPRINT_CHALLENGE;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofMinutes(5);
    }
}
