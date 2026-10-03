package top.mddata.console.service.permission.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.common.cache.console.permission.RoleDataScopeCacheKeyBuilder;
import top.mddata.console.dto.permission.RoleAppRelDto;
import top.mddata.console.entity.permission.ResourceField;
import top.mddata.console.entity.permission.ResourceMenu;
import top.mddata.console.entity.permission.RoleAppRel;
import top.mddata.console.entity.permission.RoleDataScopeRel;
import top.mddata.console.entity.permission.RoleFieldRel;
import top.mddata.console.mapper.permission.ResourceFieldMapper;
import top.mddata.console.mapper.permission.ResourceMenuMapper;
import top.mddata.console.mapper.permission.RoleAppRelMapper;
import top.mddata.console.mapper.permission.RoleDataScopeRelMapper;
import top.mddata.console.mapper.permission.RoleFieldRelMapper;
import top.mddata.console.service.permission.RoleAppRelService;
import top.mddata.console.service.permission.RoleResourceRelService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色应用关联 服务层实现。
 *
 * @author henhen6
 * @since 2025-12-03 14:54:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoleAppRelServiceImpl extends SuperServiceImpl<RoleAppRelMapper, RoleAppRel> implements RoleAppRelService {
    private final RoleResourceRelService roleResourceRelService;
    // 级联清除依赖 Mapper 而非各领域 Service：RoleDataScopeRelService/RoleFieldRelService
    // 均依赖 RoleService，而 RoleService 依赖本 Service，注入 Service 会形成构造器循环依赖
    private final RoleDataScopeRelMapper roleDataScopeRelMapper;
    private final RoleFieldRelMapper roleFieldRelMapper;
    private final ResourceMenuMapper resourceMenuMapper;
    private final ResourceFieldMapper resourceFieldMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(RoleAppRelDto dto) {
        // 级联清除同一角色+应用下的数据权限、字段权限、功能权限。
        // 功能权限删除内部的用户缓存失效（含 user_field_perm）放最后，兜住前序全部变更
        removeDataScopeByAppIds(dto.getRoleId(), dto.getAppIdList());
        removeFieldByAppIds(dto.getRoleId(), dto.getAppIdList());
        roleResourceRelService.removeByRoleIdAndAppIds(dto.getRoleId(), dto.getAppIdList());
        return super.remove(QueryWrapper.create().eq(RoleAppRel::getRoleId, dto.getRoleId()).in(RoleAppRel::getAppId, dto.getAppIdList()));
    }

    /**
     * 删除该角色在指定应用下的数据权限授权，并按涉及菜单失效 RoleDataScope 缓存
     */
    private void removeDataScopeByAppIds(Long roleId, List<Long> appIdList) {
        List<Long> menuIds = findMenuIdsByAppIds(appIdList);
        if (menuIds.isEmpty()) {
            return;
        }
        List<Long> relMenuIds = roleDataScopeRelMapper.selectListByQuery(QueryWrapper.create()
                        .select(RoleDataScopeRel::getMenuId)
                        .where(RoleDataScopeRel::getRoleId).eq(roleId)
                        .and(RoleDataScopeRel::getMenuId).in(menuIds))
                .stream().map(RoleDataScopeRel::getMenuId).toList();
        if (relMenuIds.isEmpty()) {
            return;
        }
        roleDataScopeRelMapper.deleteByQuery(QueryWrapper.create()
                .where(RoleDataScopeRel::getRoleId).eq(roleId)
                .and(RoleDataScopeRel::getMenuId).in(menuIds));
        cacheOps.del(relMenuIds.stream()
                .map(menuId -> RoleDataScopeCacheKeyBuilder.build(roleId, menuId)).toList());
    }

    /**
     * 删除该角色在指定应用下的字段受限关系。
     * 用户字段受限集缓存由功能权限删除统一失效，此处不重复处理
     */
    private void removeFieldByAppIds(Long roleId, List<Long> appIdList) {
        List<Long> menuIds = findMenuIdsByAppIds(appIdList);
        if (menuIds.isEmpty()) {
            return;
        }
        List<Long> fieldIds = resourceFieldMapper.selectListByQuery(QueryWrapper.create()
                        .select(ResourceField::getId)
                        .where(ResourceField::getMenuId).in(menuIds))
                .stream().map(ResourceField::getId).toList();
        if (fieldIds.isEmpty()) {
            return;
        }
        roleFieldRelMapper.deleteByQuery(QueryWrapper.create()
                .where(RoleFieldRel::getRoleId).eq(roleId)
                .and(RoleFieldRel::getFieldId).in(fieldIds));
    }

    private List<Long> findMenuIdsByAppIds(List<Long> appIdList) {
        return resourceMenuMapper.selectListByQuery(QueryWrapper.create()
                        .select(ResourceMenu::getId)
                        .where(ResourceMenu::getAppId).in(appIdList))
                .stream().map(ResourceMenu::getId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRoleIds(Collection<? extends Serializable> roleIdList) {
        if (CollUtil.isEmpty(roleIdList)) {
            return;
        }
        super.remove(QueryWrapper.create().in(RoleAppRel::getRoleId, roleIdList));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveByDto(RoleAppRelDto dto) {
        List<RoleAppRel> existList = list(QueryWrapper.create().eq(RoleAppRel::getRoleId, dto.getRoleId()));
        List<Long> existAppIdList = existList.stream().map(RoleAppRel::getAppId).distinct().toList();

        List<RoleAppRel> saveList = dto.getAppIdList().stream()
                // 已存在的不添加
                .filter(appId -> !existAppIdList.contains(appId))
                .map(appId -> {
                    RoleAppRel scopeRel = new RoleAppRel();
                    scopeRel.setRoleId(dto.getRoleId());
                    scopeRel.setAppId(appId);
                    return scopeRel;
                })
                .collect(Collectors.toList());

        return super.saveBatch(saveList);
    }


}
