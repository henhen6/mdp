package top.mddata.open.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.common.cache.open.AppApiCkBuilder;
import top.mddata.open.admin.entity.AppGroupRel;
import top.mddata.open.admin.entity.GroupApiRel;
import top.mddata.open.admin.entity.ScopeGroup;
import top.mddata.open.admin.mapper.AppGroupRelMapper;
import top.mddata.open.admin.mapper.GroupApiRelMapper;
import top.mddata.open.admin.mapper.ScopeGroupMapper;
import top.mddata.open.admin.service.ScopeGroupService;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 应用权限分组 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScopeGroupServiceImpl extends SuperServiceImpl<ScopeGroupMapper, ScopeGroup> implements ScopeGroupService {
    private final AppGroupRelMapper appGroupRelMapper;
    private final GroupApiRelMapper groupApiRelMapper;

    @Override
    protected void updateAfter(Object update, ScopeGroup entity) {
        delAppApiCache(Collections.singletonList(entity.getId()));
    }

    private void delAppApiCache(Collection<? extends Serializable> idList) {
        // 查询分组下的应用
        List<AppGroupRel> appGroupList = appGroupRelMapper.selectListByQuery(QueryWrapper.create().in(AppGroupRel::getGroupId, idList));
        if (CollUtil.isNotEmpty(appGroupList)) {
            // 清理应用拥有的开放接口
            List<CacheKey> keys = appGroupList.stream().map(AppGroupRel::getAppId).distinct().map(AppApiCkBuilder::builder).toList();
            cacheOps.del(keys);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        return removeByIds(Collections.singletonList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(ScopeGroup entity) {
        return removeByIds(Collections.singletonList(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        delAppApiCache(idList);

        boolean flag = super.removeByIds(idList);
        appGroupRelMapper.deleteByQuery(QueryWrapper.create().in(AppGroupRel::getGroupId, idList));
        groupApiRelMapper.deleteByQuery(QueryWrapper.create().in(GroupApiRel::getGroupId, idList));
        delCache(idList);
        return flag;
    }
}
