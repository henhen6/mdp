package top.mddata.console.permission.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Multimap;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.CollHelper;
import top.mddata.common.cache.console.permission.RoleResourceCacheKeyBuilder;
import top.mddata.console.permission.dto.RoleResourceRelDto;
import top.mddata.console.permission.entity.RoleResourceRel;
import top.mddata.console.permission.mapper.RoleResourceRelMapper;
import top.mddata.console.permission.service.RoleResourceRelService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 角色资源关联 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoleResourceRelServiceImpl extends SuperServiceImpl<RoleResourceRelMapper, RoleResourceRel> implements RoleResourceRelService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRoleResource(RoleResourceRelDto dto) {
        Long roleId = dto.getRoleId();
        Map<Long, List<Long>> appResourceMap = dto.getAppResourceMap();
        List<RoleResourceRel> roleResourceRels = mapper.selectListByQuery(QueryWrapper.create().eq(RoleResourceRel::getRoleId, roleId));

        mapper.deleteByQuery(QueryWrapper.create().eq(RoleResourceRel::getRoleId, roleId));
        List<Long> roleAppIdList = roleResourceRels.stream().map(RoleResourceRel::getAppId).toList();

        List<CacheKey> keys = new ArrayList<>();
        roleAppIdList.forEach(appId -> {
            if (!appResourceMap.containsKey(appId)) {
                keys.add(RoleResourceCacheKeyBuilder.build(roleId, appId));
            }
        });

        List<RoleResourceRel> list = new ArrayList<>();
        appResourceMap.forEach((appId, resourceIdList) -> {
            if (CollUtil.isNotEmpty(resourceIdList)) {
                resourceIdList.forEach(resourceId -> {
                    RoleResourceRel roleAppRel = new RoleResourceRel();
                    roleAppRel.setRoleId(roleId);
                    roleAppRel.setAppId(appId);
                    roleAppRel.setResourceId(resourceId);
                    list.add(roleAppRel);
                });
            }
            keys.add(RoleResourceCacheKeyBuilder.build(roleId, appId));
        });
        keys.add(RoleResourceCacheKeyBuilder.build(roleId, null));

        boolean flag = saveBatch(list);
        cacheOps.del(keys);
        return flag;
    }


    @Override
    @Transactional(readOnly = true)
    public Map<Long, Collection<Long>> findResourceIdByRoleId(Long roleId) {
        List<RoleResourceRel> list = mapper.selectListByQuery(QueryWrapper.create().eq(RoleResourceRel::getRoleId, roleId));
        Multimap<Long, Long> map = CollHelper.iterableToMultiMap(list, RoleResourceRel::getAppId, RoleResourceRel::getResourceId);
        return map.asMap();
    }
}
