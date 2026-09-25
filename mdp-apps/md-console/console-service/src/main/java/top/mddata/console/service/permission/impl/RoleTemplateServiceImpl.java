package top.mddata.console.service.permission.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mybatisflex.datapermission.DataScope;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.constant.RoleCode;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.mapper.permission.RoleMapper;
import top.mddata.console.service.organization.SystemProtectService;
import top.mddata.console.service.organization.UserRoleRelService;
import top.mddata.console.service.permission.RoleAppRelService;
import top.mddata.console.service.permission.RoleResourceRelService;
import top.mddata.console.service.permission.RoleService;
import top.mddata.console.service.permission.RoleTemplateService;

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
    private final SystemProtectService systemProtectService;

    @Override
    protected Role saveBefore(Object save) {
        Role entity = BeanUtil.toBean(save, getEntityClass());
        entity.setId(null);
        entity.setTemplateRole(true);
        ArgumentAssert.isFalse(roleService.checkCode(entity.getRoleCategory(), entity.getCode(), null), "角色编码重复");
        if (RoleCategoryEnum.PERM_SET.eq(entity.getRoleCategory())) {
            ArgumentAssert.isFalse(roleService.checkCategoryAndOrgNature(entity.getRoleCategory(), entity.getOrgNature(), null), "当前组织性质下，已存在权限集合");
        }
        // 数据范围为空视同全部（防止 null 落库绕过 DDL 默认值与"空视同 ALL"契约）
        if (StrUtil.isEmpty(entity.getDataScope())) {
            entity.setDataScope(DataScope.ALL.getCode());
        }
        return entity;
    }

    @Override
    protected Role updateBefore(Object updateDto) {
        Role entity = BeanUtil.toBean(updateDto, getEntityClass());
        entity.setTemplateRole(true);
        ArgumentAssert.isFalse(roleService.checkCode(entity.getRoleCategory(), entity.getCode(), entity.getId()), "角色编码重复");
        if (RoleCategoryEnum.PERM_SET.eq(entity.getRoleCategory())) {
            ArgumentAssert.isFalse(roleService.checkCategoryAndOrgNature(entity.getRoleCategory(), entity.getOrgNature(), entity.getId()), "当前组织性质下，已存在权限集合");
        }

        // 禁用拦截以查库旧 code 为准，运营管理员及其权限集合 template_role=true，模板页同样能触达
        Role oldRole = getById(entity.getId());
        boolean protectedRole = oldRole != null
                && (RoleCode.OPERATIONS_ADMIN.equals(oldRole.getCode())
                || RoleCode.OPERATIONS_ADMIN_COLL.equals(oldRole.getCode()));
        ArgumentAssert.isFalse(protectedRole && Boolean.FALSE.equals(entity.getState()),
                "禁用角色失败：角色[{}]受系统保护", protectedRole ? oldRole.getName() : null);

        // 受保护角色的编码、分类、组织性质是跨环境契约，禁止修改
        if (protectedRole) {
            ArgumentAssert.equals(oldRole.getCode(), entity.getCode(),
                    "修改角色失败：角色[{}]的编码不可修改", oldRole.getName());
            ArgumentAssert.equals(oldRole.getRoleCategory(), entity.getRoleCategory(),
                    "修改角色失败：角色[{}]的分类不可修改", oldRole.getName());
            ArgumentAssert.equals(oldRole.getOrgNature(), entity.getOrgNature(),
                    "修改角色失败：角色[{}]的组织性质不可修改", oldRole.getName());
        }

        // 数据范围为空视同全部（防止 null 落库绕过 DDL 默认值与"空视同 ALL"契约）
        if (StrUtil.isEmpty(entity.getDataScope())) {
            entity.setDataScope(DataScope.ALL.getCode());
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        idList.forEach(id -> systemProtectService.checkRoleNotProtected(
                Long.valueOf(String.valueOf(id)), "删除角色"));

        roleResourceRelService.removeByRoleIds(idList);
        roleAppRelService.removeByRoleIds(idList);
        userRoleRelService.removeByRoleIds(idList);

        return super.removeByIds(idList);
    }
}
