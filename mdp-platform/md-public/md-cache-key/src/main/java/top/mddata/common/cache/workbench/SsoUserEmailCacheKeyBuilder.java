package top.mddata.common.cache.workbench;


import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
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
public class SsoUserEmailCacheKeyBuilder implements CacheKeyBuilder {

    public static CacheKey builder(String name) {
        return new SsoUserEmailCacheKeyBuilder().key(name);
    }


    @Override
    public String getTable() {
        return CacheKeyTable.Workbench.USER;
    }

    @Override
    public String getField() {
        return "email";
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }

}
