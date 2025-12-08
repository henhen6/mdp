package top.mddata.console.permission.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.organization.service.UserRoleRelService;
import top.mddata.console.permission.entity.Role;
import top.mddata.console.permission.mapper.RoleMapper;
import top.mddata.console.permission.service.RoleAppRelService;
import top.mddata.console.permission.service.RoleResourceRelService;
import top.mddata.console.permission.service.RoleService;

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
public class RoleServiceImpl extends SuperServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleResourceRelService roleResourceRelService;
    private final RoleAppRelService roleAppRelService;
    private final UserRoleRelService userRoleRelService;

    @Override
    @Transactional(readOnly = true)
    public Boolean checkCode(String roleCategory, String code, Long id) {
        if (StrUtil.isEmptyIfStr(code)) {
            return true;
        }
        return mapper.selectCountByQuery(QueryWrapper.create().eq(Role::getRoleCategory, roleCategory, true).eq(Role::getCode, code).ne(Role::getId, id)) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkCategoryAndOrgNature(String roleCategory, Integer orgNature, Long id) {
        return mapper.selectCountByQuery(QueryWrapper.create().eq(Role::getRoleCategory, roleCategory, true).eq(Role::getOrgNature, orgNature, true).ne(Role::getId, id)) > 0;
    }

    @Override
    protected Role saveBefore(Object save) {
        Role entity = BeanUtil.toBean(save, getEntityClass());
        ArgumentAssert.isFalse(checkCode(entity.getRoleCategory(), entity.getCode(), null), "角色编码重复");
        entity.setId(null);
        entity.setOrgNature(OrgNatureEnum.DEFAULT.getCode());
        entity.setTemplateRole(false);
        entity.setRoleCategory(RoleCategoryEnum.NORMAL_ROLE.getCode());
        return entity;
    }

    @Override
    protected Role updateBefore(Object updateDto) {
        Role entity = BeanUtil.toBean(updateDto, getEntityClass());
        ArgumentAssert.isFalse(checkCode(entity.getRoleCategory(), entity.getCode(), entity.getId()), "角色编码重复");
        entity.setOrgNature(OrgNatureEnum.DEFAULT.getCode());
        entity.setTemplateRole(false);
        entity.setRoleCategory(RoleCategoryEnum.NORMAL_ROLE.getCode());
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
