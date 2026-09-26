package top.mddata.common.cache.console.permission;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 菜单数据权限开关 KEY
 *
 * @author henhen6
 * @since 2026-09-26
 */
public class MenuDataScopeCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(String code) {
        return new MenuDataScopeCacheKeyBuilder().key(code);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Console.RESOURCE_MENU;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }
}
