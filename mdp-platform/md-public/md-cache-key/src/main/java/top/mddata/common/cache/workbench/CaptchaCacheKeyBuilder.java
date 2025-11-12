package top.mddata.common.cache.workbench;


import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 参数 KEY
 * <p>
 *
 * @author henhen6
 * @date 2020/9/20 6:45 下午
 */
public class CaptchaCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(String key, String template) {
        return new CaptchaCacheKeyBuilder().key(key, template);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.CAPTCHA;
    }


    @Override
    public Duration getExpire() {
        return Duration.ofMinutes(15);
    }
}
