
package top.mddata.base.captcha.slider.autoconfigure.cache;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import com.anji.captcha.service.CaptchaCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.mddata.base.cache.redis.CacheResult;
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
@Slf4j
public class SliderCaptchaCacheServiceImpl implements CaptchaCacheService {
    private final CacheOps cacheOps;

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        log.info("行为验证码设置缓存: key={}, value={}, expiresInSeconds={}", key, value, expiresInSeconds);
        CacheKey cacheKey = new CacheKey(key, Duration.ofSeconds(expiresInSeconds));
        if (NumberUtil.isNumber(value)) {
            cacheOps.set(cacheKey, Convert.toInt(value));
        } else {
            cacheOps.set(cacheKey, value);
        }
    }

    @Override
    public boolean exists(String key) {
        log.info("行为验证码判断缓存是否存在: key={}", key);
        CacheKey cacheKey = new CacheKey(key);
        return cacheOps.exists(cacheKey);
    }

    @Override
    public void delete(String key) {
        log.info("行为验证码删除缓存: key={}", key);
        cacheOps.del(key);
    }

    @Override
    public String get(String key) {
        log.info("行为验证码获取缓存: key={}", key);
        CacheResult<Object> result = cacheOps.get(key);
        return Convert.toStr(result.getValue());
    }

    @Override
    public String type() {
        return StorageType.REDIS.name().toLowerCase();
    }

    @Override
    public Long increment(String key, long val) {
        log.info("行为验证码缓存自增: key={}, val={}", key, val);
        CacheKey cacheKey = new CacheKey(key);
        return cacheOps.incr(cacheKey);
    }
}
