package top.mddata.gateway.sop.manager.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.common.cache.open.AppByAppKeyCkBuilder;
import top.mddata.common.cache.open.AppCkBuilder;
import top.mddata.common.enumeration.BooleanEnum;
import top.mddata.gateway.sop.dao.AppMapper;
import top.mddata.gateway.sop.dao.GroupApiRelMapper;
import top.mddata.gateway.sop.manager.AppManager;
import top.mddata.gateway.sop.pojo.dto.ApiDto;
import top.mddata.gateway.sop.pojo.dto.AppDto;
import top.mddata.gateway.sop.pojo.entity.App;
import top.mddata.gateway.sop.pojo.entity.GroupApiRel;

import java.util.Objects;

/**
 *
 * @author tangyh
 * @since 2026/1/6 16:47
 */
@Service
public class AppManagerImpl implements AppManager {
    @Resource
    private AppMapper mapper;
    @Resource
    private GroupApiRelMapper groupApiRelMapper;
    @Resource
    private CacheOps cacheOps;

    @Override
    @Transactional(readOnly = true)
    public AppDto getByAppKey(String appKey) {
        CacheKey idKey = AppByAppKeyCkBuilder.builder(appKey);
        CacheResult<Long> appIdCache = cacheOps.get(idKey, (k) -> {
            App app = mapper.selectOneByQuery(QueryWrapper.create().eq(App::getAppKey, appKey));
            return app != null ? app.getId() : null;
        });

        if (appIdCache.isNullVal()) {
            return null;
        }
        Long appId = appIdCache.asLong();

        CacheKey entityKey = AppCkBuilder.builder(appId);
        CacheResult<App> apiCache = cacheOps.get(entityKey, (k) -> mapper.selectOneById(appId));

        return BeanUtil.toBean(apiCache.getValue(), AppDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long id, ApiDto apiDto) {
        // 通用接口都可以访问
        if (BooleanEnum.FALSE.eq(apiDto.getPermission())) {
            return true;
        }

        QueryWrapper.create().select().from(GroupApiRel.class)
                        .innerJoin(ScopeGroup.class)
                                .

        groupApiRelMapper.selectListByQuery();

        return false;
    }
}
