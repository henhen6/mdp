package top.mddata.console.service.permission.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.console.permission.UserFieldPermCacheKeyBuilder;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.common.mapper.UserRoleRelMapper;
import top.mddata.console.dto.permission.RoleFieldRelDto;
import top.mddata.console.entity.permission.ResourceField;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.entity.permission.RoleFieldRel;
import top.mddata.console.mapper.permission.ResourceFieldMapper;
import top.mddata.console.mapper.permission.RoleFieldRelMapper;
import top.mddata.console.service.permission.RoleFieldRelService;
import top.mddata.console.service.permission.RoleService;
import top.mddata.console.vo.permission.ResourceFieldVo;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final ResourceFieldMapper resourceFieldMapper;
    private final RoleService roleService;

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
        Role targetRole = roleService.getById(roleId);
        ArgumentAssert.notNull(targetRole, "角色[{}]不存在", roleId);
        validateAssignable(dto, targetRole);

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
    @Transactional(readOnly = true)
    public List<ResourceFieldVo> findAssignableFieldList() {
        Role permSet = roleService.getPermSetRoleOfCurrentOperator();
        if (permSet == null) {
            return List.of();
        }
        QueryWrapper inWrapper = QueryWrapper.create()
                .select(RoleFieldRel::getFieldId).from(RoleFieldRel.class)
                .where(RoleFieldRel::getRoleId).eq(permSet.getId());
        QueryWrapper wrapper = QueryWrapper.create().from(ResourceField.class)
                .where(ResourceField::getState).eq(true)
                .and(ResourceField::getId).in(inWrapper);
        return resourceFieldMapper.selectListByQueryAs(wrapper, ResourceFieldVo.class);
    }

    /**
     * 校验普通角色的字段授权不超出权限集合的字段池；
     * 模板角色（权限集合/管理员角色）是字段池的定义方，豁免校验
     */
    private void validateAssignable(RoleFieldRelDto dto, Role targetRole) {
        if (!RoleCategoryEnum.NORMAL_ROLE.getCode().equals(targetRole.getRoleCategory())
                || CollUtil.isEmpty(dto.getFieldIdList())) {
            return;
        }
        Role permSet = roleService.getPermSetRoleOfCurrentOperator();
        ArgumentAssert.notNull(permSet, "当前组织性质未配置权限集合角色，无法分配字段权限");

        Set<Long> pool = new HashSet<>(findFieldIdsByRoleId(permSet.getId()));
        List<Long> exceeded = dto.getFieldIdList().stream()
                .filter(fieldId -> !pool.contains(fieldId)).distinct().toList();
        if (CollUtil.isNotEmpty(exceeded)) {
            List<String> names = resourceFieldMapper.selectListByIds(exceeded).stream()
                    .map(ResourceField::getName).toList();
            ArgumentAssert.isTrue(false, "字段[{}]超出权限集合可分配范围", CollUtil.join(names, "、"));
        }
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
