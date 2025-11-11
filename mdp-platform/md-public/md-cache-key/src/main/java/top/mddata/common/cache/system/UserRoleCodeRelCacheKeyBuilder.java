package top.mddata.common.cache.system;


import top.mddata.common.cache.CacheKeyModular;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;

import java.time.Duration;

/**
 * 用户拥有的角色编码 KEY
 * <p>
 *
 * @author henhen6
 * @since 2024年08月23日16:31:03
 */
public class UserRoleCodeRelCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(Long roleId) {
        return new UserRoleCodeRelCacheKeyBuilder().key(roleId);
    }

    @Override
    public String getPrefix() {
        return CacheKeyModular.PREFIX;
    }

    @Override
    public String getTable() {
        return "user_role:code";
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }

}
