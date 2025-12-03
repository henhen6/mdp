package top.mddata.common.cache.console.system;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyModular;
import top.mddata.common.cache.CacheKeyTable;

/**
 * 系统参数缓存
 * 根据 参数的uniqKey 缓存 参数的id
 *
 * key: SYS_PARAM:{uniqKey}
 * value: id
 *
 * @author henhen6
 * @since 2025/8/6 23:55
 */
public class ConfigUniqKeyCacheKeyBuilder implements CacheKeyBuilder {

    /**
     * 构造器
     * @param uniqKey 参数标识
     * @return key
     */
    public static CacheKey builder(String uniqKey) {
        return new ConfigUniqKeyCacheKeyBuilder().key(uniqKey);
    }

    @Override
    public String getPrefix() {
        return CacheKeyModular.PREFIX;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Console.PARAM;
    }

    @Override
    public String getField() {
        return "uniqKey";
    }
}
