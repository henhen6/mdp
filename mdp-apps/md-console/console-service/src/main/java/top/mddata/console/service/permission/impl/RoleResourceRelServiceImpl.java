package top.mddata.console.service.permission.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.google.common.collect.Multimap;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.CollHelper;
import top.mddata.common.cache.console.permission.RoleResourceCacheKeyBuilder;
import top.mddata.common.cache.console.permission.UserFieldPermCacheKeyBuilder;
import top.mddata.common.cache.console.permission.UserResourceApiCacheKeyBuilder;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.mapper.UserRoleRelMapper;
import top.mddata.console.dto.permission.RoleResourceRelDto;
import top.mddata.console.entity.permission.RoleResourceRel;
import top.mddata.console.mapper.permission.RoleResourceRelMapper;
import top.mddata.console.service.permission.RoleResourceRelService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final UserRoleRelMapper userRoleRelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRoleIds(Collection<? extends Serializable> roleIdList) {
        if (CollUtil.isEmpty(roleIdList)) {
            return;
        }

        List<RoleResourceRel> roleResourceRels = mapper.selectListByQuery(QueryWrapper.create().in(RoleResourceRel::getRoleId, roleIdList));

        super.remove(QueryWrapper.create().in(RoleResourceRel::getRoleId, roleIdList));

        List<CacheKey> keys = new ArrayList<>();
        List<Long> roleAppIdList = roleResourceRels.stream().map(RoleResourceRel::getAppId).toList();
        roleAppIdList.forEach(appId -> roleIdList.forEach(roleId -> keys.add(RoleResourceCacheKeyBuilder.build(Convert.toLong(roleId), appId))));
        roleIdList.forEach(roleId -> keys.add(RoleResourceCacheKeyBuilder.build(Convert.toLong(roleId), null)));
        cacheOps.del(keys);

        // 授权变更影响这些角色下所有用户的接口放行集
        invalidateUserResourceApiCacheByRoleIds(roleIdList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRoleIdAndAppIds(Long roleId, List<Long> appIdList) {
        if (roleId == null || CollUtil.isEmpty(appIdList)) {
            return;
        }

        super.remove(QueryWrapper.create().eq(RoleResourceRel::getRoleId, roleId).in(RoleResourceRel::getAppId, appIdList));

        List<CacheKey> keys = new ArrayList<>();
        appIdList.forEach(appId -> keys.add(RoleResourceCacheKeyBuilder.build(Convert.toLong(roleId), appId)));
        keys.add(RoleResourceCacheKeyBuilder.build(Convert.toLong(roleId), null));
        cacheOps.del(keys);

        // 授权变更影响该角色下所有用户的接口放行集
        invalidateUserResourceApiCacheByRoleIds(List.of(roleId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRoleResource(RoleResourceRelDto dto) {
        Long roleId = dto.getRoleId();
        Boolean batch = dto.getBatch() != null && dto.getBatch();
        Map<Long, List<Long>> appResourceMap = dto.getAppResourceMap();
        List<RoleResourceRel> roleResourceRels = mapper.selectListByQuery(QueryWrapper.create().eq(RoleResourceRel::getRoleId, roleId));

        if (batch) {
            mapper.deleteByQuery(QueryWrapper.create().eq(RoleResourceRel::getRoleId, roleId));
        } else {
            Set<Long> appIds = appResourceMap.keySet();
            ArgumentAssert.notEmpty(appIds, "应用不能为空");

            mapper.deleteByQuery(QueryWrapper.create().eq(RoleResourceRel::getRoleId, roleId).in(RoleResourceRel::getAppId, appIds));
        }

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

        // 授权变更影响该角色下所有用户的接口放行集
        invalidateUserResourceApiCacheByRoleIds(List.of(roleId));

        return flag;
    }


    @Override
    public void invalidateUserResourceApiCacheByRoleIds(Collection<? extends Serializable> roleIdList) {
        if (CollUtil.isEmpty(roleIdList)) {
            return;
        }
        List<Long> userIds = userRoleRelMapper.selectListByQuery(QueryWrapper.create()
                        .select(UserRoleRel::getUserId)
                        .where(UserRoleRel::getRoleId).in(roleIdList))
                .stream().map(UserRoleRel::getUserId).distinct().toList();
        List<CacheKey> userKeys = userIds.stream()
                .map(UserResourceApiCacheKeyBuilder::build)
                .toList();
        // 角色资源/角色本身变更同样影响这些用户的字段受限集
        userIds.stream().map(UserFieldPermCacheKeyBuilder::build).forEach(userKeys::add);
        if (!userKeys.isEmpty()) {
            cacheOps.del(userKeys);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Collection<Long>> findResourceIdByRoleId(Long roleId) {
        List<RoleResourceRel> list = mapper.selectListByQuery(QueryWrapper.create().eq(RoleResourceRel::getRoleId, roleId));
        Multimap<Long, Long> map = CollHelper.iterableToMultiMap(list, RoleResourceRel::getAppId, RoleResourceRel::getResourceId);
        return map.asMap();
    }
}
