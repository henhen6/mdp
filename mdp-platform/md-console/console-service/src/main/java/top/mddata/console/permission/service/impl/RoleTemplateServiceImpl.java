package top.mddata.console.permission.service.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.organization.service.UserRoleRelService;
import top.mddata.console.permission.entity.Role;
import top.mddata.console.permission.mapper.RoleMapper;
import top.mddata.console.permission.service.RoleAppRelService;
import top.mddata.console.permission.service.RoleResourceRelService;
import top.mddata.console.permission.service.RoleService;
import top.mddata.console.permission.service.RoleTemplateService;

import java.io.Serializable;
import java.util.Collection;

/**
 * 角色 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoleTemplateServiceImpl extends SuperServiceImpl<RoleMapper, Role> implements RoleTemplateService {

    private final RoleService roleService;
    private final RoleResourceRelService roleResourceRelService;
    private final RoleAppRelService roleAppRelService;
    private final UserRoleRelService userRoleRelService;

    @Override
    protected Role saveBefore(Object save) {
        Role entity = BeanUtil.toBean(save, getEntityClass());
        entity.setId(null);
        entity.setTemplateRole(true);
        ArgumentAssert.isFalse(roleService.checkCode(entity.getCode(), null), "角色编码重复");
        return entity;
    }

    @Override
    protected Role updateBefore(Object updateDto) {
        Role entity = BeanUtil.toBean(updateDto, getEntityClass());
        entity.setTemplateRole(true);
        ArgumentAssert.isFalse(roleService.checkCode(entity.getCode(), entity.getId()), "角色编码重复");
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        roleResourceRelService.removeByRoleIds(idList);
        roleAppRelService.removeByRoleIds(idList);
        userRoleRelService.removeByRoleIds(idList);

        return super.removeByIds(idList);
    }
}
