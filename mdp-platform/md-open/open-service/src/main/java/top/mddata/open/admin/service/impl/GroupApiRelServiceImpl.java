package top.mddata.open.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.open.AppApiCkBuilder;
import top.mddata.open.admin.dto.GroupApiRelDto;
import top.mddata.open.admin.entity.AppGroupRel;
import top.mddata.open.admin.entity.GroupApiRel;
import top.mddata.open.admin.mapper.AppGroupRelMapper;
import top.mddata.open.admin.mapper.GroupApiRelMapper;
import top.mddata.open.admin.service.GroupApiRelService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分组拥有的对外接口 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GroupApiRelServiceImpl extends SuperServiceImpl<GroupApiRelMapper, GroupApiRel> implements GroupApiRelService {
    private final AppGroupRelMapper appGroupRelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupApiRel saveDto(Object save) {
        GroupApiRelDto dto = (GroupApiRelDto) save;
        Long groupId = dto.getGroupId();
        List<Long> apiIdList = dto.getApiIdList();
        if (CollectionUtils.isEmpty(apiIdList)) {
            return null;
        }

        List<AppGroupRel> appGroupList = appGroupRelMapper.selectListByQuery(QueryWrapper.create().eq(AppGroupRel::getGroupId, groupId));

        List<GroupApiRel> existList = list(QueryWrapper.create().eq(GroupApiRel::getGroupId, groupId));
        List<Long> existApiIdList = existList.stream().map(GroupApiRel::getApiId).distinct().toList();

        List<GroupApiRel> saveList = apiIdList.stream()
                // 已存在的不添加
                .filter(apiId -> !existApiIdList.contains(apiId))
                .map(apiId -> {
                    GroupApiRel apiRel = new GroupApiRel();
                    apiRel.setGroupId(groupId);
                    apiRel.setApiId(apiId);
                    return apiRel;
                })
                .collect(Collectors.toList());
        super.saveBatch(saveList);

        if (CollUtil.isNotEmpty(appGroupList)) {
            // 应用拥有的开放接口
            List<CacheKey> keys = appGroupList.stream().map(AppGroupRel::getAppId).distinct().map(AppApiCkBuilder::builder).toList();
            cacheOps.del(keys);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long groupId, List<Long> apiIdList) {
        ArgumentAssert.notNull(groupId, "权限分组ID不能为空");
        ArgumentAssert.notEmpty(apiIdList, "接口ID不能为空");

        List<AppGroupRel> appGroupList = appGroupRelMapper.selectListByQuery(QueryWrapper.create().eq(AppGroupRel::getGroupId, groupId));
        boolean cnt = super.remove(QueryWrapper.create().eq(GroupApiRel::getGroupId, groupId).in(GroupApiRel::getApiId, apiIdList));

        if (CollUtil.isNotEmpty(appGroupList)) {
            // 应用拥有的开放接口
            List<CacheKey> keys = appGroupList.stream().map(AppGroupRel::getAppId).distinct().map(AppApiCkBuilder::builder).toList();
            cacheOps.del(keys);
        }
        return cnt;
    }
}
