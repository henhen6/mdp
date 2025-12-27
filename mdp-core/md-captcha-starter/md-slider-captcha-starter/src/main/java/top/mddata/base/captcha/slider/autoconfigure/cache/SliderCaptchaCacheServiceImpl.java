
package top.mddata.base.captcha.slider.autoconfigure.cache;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import com.anji.captcha.service.CaptchaCacheService;
import lombok.RequiredArgsConstructor;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.captcha.slider.enumeration.StorageType;
import top.mddata.base.model.cache.CacheKey;

import java.time.Duration;

/**
 * 行为验证码 Redis 缓存实现
 *
 * @author henhen
 */
@RequiredArgsConstructor
public class SliderCaptchaCacheServiceImpl implements CaptchaCacheService {
    private final CacheOps cacheOps;

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        CacheKey cacheKey = new CacheKey(key, Duration.ofSeconds(expiresInSeconds));
        if (NumberUtil.isNumber(value)) {
            cacheOps.set(cacheKey, Convert.toInt(value));
        } else {
            cacheOps.set(cacheKey, value);
        }
    }

    @Override
    public boolean exists(String key) {
        CacheKey cacheKey = new CacheKey(key);
        return cacheOps.exists(cacheKey);
    }

    @Override
    public void delete(String key) {
        cacheOps.del(key);
    }

    @Override
    public String get(String key) {
        return Convert.toStr(cacheOps.get(key));
    }

    @Override
    public String type() {
        return StorageType.REDIS.name().toLowerCase();
    }

    @Override
    public Long increment(String key, long val) {
        CacheKey cacheKey = new CacheKey(key);
        return cacheOps.incr(cacheKey);
    }
}
