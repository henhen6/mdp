package top.mddata.console.organization.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.console.organization.UserOrgCacheKeyBuilder;
import top.mddata.common.entity.UserOrgRel;
import top.mddata.common.mapper.UserOrgRelMapper;
import top.mddata.console.organization.service.UserOrgRelService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 用户所属组织 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserOrgRelServiceImpl extends SuperServiceImpl<UserOrgRelMapper, UserOrgRel> implements UserOrgRelService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByUserIds(Collection<Long> userIds) {
        ArgumentAssert.notEmpty(userIds, "用户ID不能为空");
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in(UserOrgRel::getUserId, userIds);

        boolean remove = remove(queryWrapper);
        cacheOps.del(userIds.stream().map(UserOrgCacheKeyBuilder::build).toList());
        return remove;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByOrgIds(Collection<? extends Serializable> idList) {
        if (CollUtil.isEmpty(idList)) {
            return false;
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in(UserOrgRel::getOrgId, idList);

        List<UserOrgRel> list = list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        boolean remove = remove(queryWrapper);

        cacheOps.del(list.stream().map(UserOrgRel::getUserId).distinct().map(UserOrgCacheKeyBuilder::build).toList());
        return remove;
    }
}
