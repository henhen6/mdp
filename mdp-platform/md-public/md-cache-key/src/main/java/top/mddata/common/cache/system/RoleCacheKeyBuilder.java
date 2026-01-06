package top.mddata.common.cache.system;


import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;

import java.time.Duration;

/**
 * 角色 KEY
 * <p>
 *
 * @author henhen6
 * @since 2024年08月23日16:31:03
 */
public class RoleCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(Long roleId) {
        return new RoleCacheKeyBuilder().key(roleId);
    }




    @Override
    public String getTable() {
        return "role";
    }


    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }

}
