package top.mddata.common.cache.console.system;

import top.mddata.common.cache.CacheKeyModular;
import top.mddata.common.cache.CacheKeyTable;
import top.mddata.base.model.cache.CacheHashKey;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;

import java.io.Serializable;

/**
 * 字典项 hash 缓存
 *
 * 使用hash缓存，根据字典key + 字典项key， 缓存字典项对象
 *
 * key: SYS_DICT_ITEM:{dictKey}
 * field: {dictItemKey}
 * value: 字典项对象
 *
 * @author henhen6
 * @since 2025/8/6 23:55
 */
public class SysDictItemHashCacheKeyBuilder implements CacheKeyBuilder {

    /**
     * 构造器
     * @param dictKey 字典key
     * @return HashKey
     */
    public static CacheKey builder(Serializable dictKey) {
        return new SysDictItemHashCacheKeyBuilder().hashKey(dictKey);
    }

    /**
     * 构造器
     * @param dictKey 字典key
     * @param dictItemKey 字典项key
     * @return HashKey
     */
    public static CacheHashKey builder(String dictKey, String dictItemKey) {
        return new SysDictItemHashCacheKeyBuilder().hashFieldKey(dictItemKey, dictKey);
    }

    @Override
    public String getPrefix() {
        return CacheKeyModular.PREFIX;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Admin.SYS_DICT_ITEM;
    }

    @Override
    public String getField() {
        return "uniqKey";
    }

}
