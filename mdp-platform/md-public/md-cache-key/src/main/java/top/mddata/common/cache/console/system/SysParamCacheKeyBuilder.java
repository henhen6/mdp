package top.mddata.common.cache.console.system;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyModular;
import top.mddata.common.cache.CacheKeyTable;

/**
 * 系统参数 缓存
 *
 * 根据 参数的id 缓存 参数对象
 *
 * key: SYS_PARAM:{id}
 * value: SysParam
 *
 * @author henhen6
 * @since 2025/8/6 23:55
 */
public class SysParamCacheKeyBuilder implements CacheKeyBuilder {

    /**
     * 构造器
     * @param id 参数id
     * @return key
     */
    public static CacheKey builder(Long id) {
        return new SysParamCacheKeyBuilder().key(id);
    }

    @Override
    public String getPrefix() {
        return CacheKeyModular.PREFIX;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Admin.SYS_PARAM;
    }

}
