package top.mddata.common.cache.workbench;


import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyModular;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 系统用户 KEY
 * <p>
 * #sso_user
 *
 * @author henhen6
 * @date 2025年07月09日12:37:18
 */
public class SsoUserPhoneCacheKeyBuilder implements CacheKeyBuilder {

    public static CacheKey builder(String name) {
        return new SsoUserPhoneCacheKeyBuilder().key(name);
    }

    @Override
    public String getPrefix() {
        return CacheKeyModular.PREFIX;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Center.SSO_USER;
    }

    @Override
    public String getField() {
        return "phone";
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }

}
