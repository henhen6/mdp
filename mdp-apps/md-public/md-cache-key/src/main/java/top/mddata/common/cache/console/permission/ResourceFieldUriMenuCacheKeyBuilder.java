package top.mddata.common.cache.console.permission;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 接口URI → 字段权限菜单 预解析映射 KEY
 *
 * @author henhen6
 */
public class ResourceFieldUriMenuCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build() {
        return new ResourceFieldUriMenuCacheKeyBuilder().key();
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Console.RESOURCE_FIELD_URI_MENU;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }

}
