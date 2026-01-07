package top.mddata.gateway.sop.manager.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.common.cache.open.ApiByMethodVersionCkBuilder;
import top.mddata.common.cache.open.ApiCkBuilder;
import top.mddata.gateway.sop.common.ApiDto;
import top.mddata.gateway.sop.manager.ApiManager;
import top.mddata.open.admin.entity.Api;
import top.mddata.open.admin.mapper.ApiMapper;

/**
 *
 * @author henhen
 * @since 2026/1/6 16:47
 */
@Service
public class ApiManagerImpl implements ApiManager {
    @Resource
    private ApiMapper apiMapper;
    @Resource
    private CacheOps cacheOps;


    @Override
    @Transactional(readOnly = true)
    public ApiDto getByMethodAndVersion(String method, String version) {
        /*
        1. 通过 method + version 去缓存中获取id
        2. 通过 id 去缓存中获取api
        3. 新增api时，需要将 ApiByMethodVersionCkBuilder 缓存清理
         */
//        todo 测试一下，先缓存一个空值， 然后新开发一个同名的方法，测试是否能正常调用
        CacheKey idKey = ApiByMethodVersionCkBuilder.builder(method, version);
        CacheResult<Long> apiIdCache = cacheOps.get(idKey, (k) -> {
            Api api = apiMapper.selectOneByQuery(QueryWrapper.create().eq(Api::getMethodName, method).eq(Api::getApiVersion, version));
            return api != null ? api.getId() : null;
        });

        if (apiIdCache.isNullVal()) {
            return null;
        }
        Long apiId = apiIdCache.asLong();

        CacheKey entityKey = ApiCkBuilder.builder(apiId);
        CacheResult<Api> apiCache = cacheOps.get(entityKey, (k) -> apiMapper.selectOneById(apiId));

        return BeanUtil.toBean(apiCache.getValue(), ApiDto.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(Api apiInfo) {
        apiMapper.insertOrUpdate(apiInfo);

        cacheOps.set(ApiByMethodVersionCkBuilder.builder(apiInfo.getMethodName(), apiInfo.getApiVersion()), apiInfo.getId());
        cacheOps.set(ApiCkBuilder.builder(apiInfo.getId()), apiInfo);
    }
}
