package top.mddata.common.cache.workbench;

import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 指纹录入前密码验证票据 KEY（一次性消费，5 分钟过期）
 *
 * @author tangyh
 * @date 2026-11-13
 */
public class FingerprintPwdVerifiedCacheKeyBuilder implements CacheKeyBuilder {

    @Override
    public String getTable() {
        return CacheKeyTable.Workbench.FINGERPRINT_PWD_VERIFIED;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofMinutes(5);
    }
}
