package top.mddata.open.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.common.cache.open.ApiCkBuilder;
import top.mddata.open.admin.entity.Api;
import top.mddata.open.admin.mapper.ApiMapper;
import top.mddata.open.admin.service.ApiService;

/**
 * 开放接口 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ApiServiceImpl extends SuperServiceImpl<ApiMapper, Api> implements ApiService {
    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new ApiCkBuilder();
    }
}
