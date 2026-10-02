package top.mddata.console.service.permission.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.common.cache.console.permission.UserFieldPermCacheKeyBuilder;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.mapper.UserRoleRelMapper;
import top.mddata.console.dto.permission.RoleFieldRelDto;
import top.mddata.console.entity.permission.RoleFieldRel;
import top.mddata.console.mapper.permission.RoleFieldRelMapper;
import top.mddata.console.service.permission.RoleFieldRelService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 角色字段受限关系 服务层实现。
 *
 * @author henhen6
 * @since 2026-10-02
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoleFieldRelServiceImpl extends SuperServiceImpl<RoleFieldRelMapper, RoleFieldRel> implements RoleFieldRelService {
    private final UserRoleRelMapper userRoleRelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Long> findFieldIdsByRoleId(Long roleId) {
        return mapper.selectListByQuery(QueryWrapper.create()
                        .select(RoleFieldRel::getFieldId)
                        .where(RoleFieldRel::getRoleId).eq(roleId))
                .stream().map(RoleFieldRel::getFieldId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRoleField(RoleFieldRelDto dto) {
        Long roleId = dto.getRoleId();
        mapper.deleteByQuery(QueryWrapper.create().where(RoleFieldRel::getRoleId).eq(roleId));

        boolean flag = true;
        if (CollUtil.isNotEmpty(dto.getFieldIdList())) {
            List<RoleFieldRel> list = dto.getFieldIdList().stream().distinct()
                    .map(fieldId -> {
                        RoleFieldRel rel = new RoleFieldRel();
                        rel.setRoleId(roleId);
                        rel.setFieldId(fieldId);
                        return rel;
                    })
                    .toList();
            flag = saveBatch(list);
        }

        // 授权变更影响该角色下所有用户的字段受限集
        invalidateUserFieldPermCacheByRoleIds(List.of(roleId));
        return flag;
    }

    @Override
    public void invalidateUserFieldPermCacheByRoleIds(Collection<? extends Serializable> roleIdList) {
        if (CollUtil.isEmpty(roleIdList)) {
            return;
        }
        List<Long> userIds = userRoleRelMapper.selectListByQuery(QueryWrapper.create()
                        .select(UserRoleRel::getUserId)
                        .where(UserRoleRel::getRoleId).in(roleIdList))
                .stream().map(UserRoleRel::getUserId).distinct().toList();
        delUserCache(userIds);
    }

    @Override
    public void invalidateUserFieldPermCacheByFieldIds(Collection<Long> fieldIdList) {
        if (CollUtil.isEmpty(fieldIdList)) {
            return;
        }
        List<Long> roleIds = mapper.selectListByQuery(QueryWrapper.create()
                        .select(RoleFieldRel::getRoleId)
                        .where(RoleFieldRel::getFieldId).in(fieldIdList))
                .stream().map(RoleFieldRel::getRoleId).distinct().toList();
        invalidateUserFieldPermCacheByRoleIds(roleIds);
    }

    private void delUserCache(Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        List<CacheKey> keys = userIds.stream().map(UserFieldPermCacheKeyBuilder::build).toList();
        cacheOps.del(keys);
    }
}
