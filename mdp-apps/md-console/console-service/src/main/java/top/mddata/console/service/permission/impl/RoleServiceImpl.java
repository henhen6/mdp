package top.mddata.console.service.permission.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.mybatisflex.datascope.model.DataScopeEnum;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.console.permission.RoleDataScopeCacheKeyBuilder;
import top.mddata.common.constant.RoleCode;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.entity.permission.RoleAppRel;
import top.mddata.console.entity.permission.RoleDataScopeRel;
import top.mddata.console.entity.permission.RoleFieldRel;
import top.mddata.console.mapper.permission.RoleDataScopeRelMapper;
import top.mddata.console.mapper.permission.RoleFieldRelMapper;
import top.mddata.console.mapper.permission.RoleMapper;
import top.mddata.console.service.organization.SystemProtectService;
import top.mddata.console.service.organization.UserRoleRelService;
import top.mddata.console.service.permission.RoleAppRelService;
import top.mddata.console.service.permission.RoleResourceRelService;
import top.mddata.console.service.permission.RoleService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

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
    private final SystemProtectService systemProtectService;
    private final RoleDataScopeRelMapper roleDataScopeRelMapper;
    // 级联删除依赖 Mapper 而非领域 Service：RoleDataScopeRelService/RoleFieldRelService
    // 均依赖本 Service（授权校验），注入 Service 会形成构造器循环依赖
    private final RoleFieldRelMapper roleFieldRelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<String> findUserRoleCodes(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create().select().from(Role.class)
                .innerJoin(UserRoleRel.class).on(Role::getId, UserRoleRel::getRoleId).eq(Role::getState, true)
                .where(UserRoleRel::getUserId).eq(userId);
        List<Role> list = list(wrapper);
        return list.stream().map(Role::getCode).toList();
    }

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
    public Role getByCode(String code) {
        if (StrUtil.isEmptyIfStr(code)) {
            return null;
        }
        return mapper.selectOneByQuery(QueryWrapper.create().eq(Role::getCode, code).eq(Role::getState, true));
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkCategoryAndOrgNature(String roleCategory, Integer orgNature, Long id) {
        return mapper.selectCountByQuery(QueryWrapper.create().eq(Role::getRoleCategory, roleCategory, true).eq(Role::getOrgNature, orgNature, true).ne(Role::getId, id)) > 0;
    }

    /**
     * 取当前用户顶级机构的组织性质，取不到默认总公司
     */
    private Integer resolveCurrentOrgNature() {
        Integer nature = ContextUtil.getCurrentTopCompanyNature();
        return nature != null ? nature : OrgNatureEnum.HEAD_COMPANY.getCode();
    }

    @Override
    protected Role saveBefore(Object save) {
        Role entity = BeanUtil.toBean(save, getEntityClass());
        ArgumentAssert.isFalse(checkCode(entity.getRoleCategory(), entity.getCode(), null), "角色编码重复");
        ArgumentAssert.isFalse(RoleCode.BUILT_IN_CODES.contains(entity.getCode()),
                "角色编码[{}]是系统内置保留编码，不可使用", entity.getCode());
        entity.setId(null);
        entity.setOrgNature(resolveCurrentOrgNature());
        entity.setTemplateRole(false);
        entity.setRoleCategory(RoleCategoryEnum.NORMAL_ROLE.getCode());
        return entity;
    }

    @Override
    protected Role updateBefore(Object updateDto) {
        Role entity = BeanUtil.toBean(updateDto, getEntityClass());
        ArgumentAssert.isFalse(checkCode(entity.getRoleCategory(), entity.getCode(), entity.getId()), "角色编码重复");
        ArgumentAssert.isFalse(RoleCode.BUILT_IN_CODES.contains(entity.getCode()),
                "角色编码[{}]是系统内置保留编码，不可使用", entity.getCode());

        // 角色管理只允许维护普通角色，管理员角色与权限集合请到角色模板页面维护
        Role oldRole = getById(entity.getId());
        ArgumentAssert.notNull(oldRole, "角色[{}]不存在", entity.getId());
        ArgumentAssert.isTrue(
                RoleCategoryEnum.NORMAL_ROLE.getCode().equals(oldRole.getRoleCategory()),
                "修改角色失败：角色[{}]不是普通角色，请到角色模板页面维护", oldRole.getName());

        entity.setOrgNature(resolveCurrentOrgNature());
        entity.setTemplateRole(false);
        entity.setRoleCategory(RoleCategoryEnum.NORMAL_ROLE.getCode());

        // 禁用拦截以查库旧 code 为准，不依赖前端回传的 code
        boolean protectedRoleDisabled = RoleCode.OPERATIONS_ADMIN.equals(oldRole.getCode())
                                        && Boolean.FALSE.equals(entity.getState());
        ArgumentAssert.isFalse(protectedRoleDisabled, "禁用角色失败：角色[运营管理员]受系统保护");

        return entity;
    }

    @Override
    protected void updateAfter(Object updateDto, Role entity) {
        // 角色任何更新（含停用/启用）都失效该角色下所有用户的接口放行集缓存B
        // 角色更新为低频操作，全量失效可接受
        roleResourceRelService.invalidateUserResourceApiCacheByRoleIds(List.of(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        idList.forEach(id -> systemProtectService.checkRoleNotProtected(
                Long.valueOf(String.valueOf(id)), "删除角色"));

        // 级联清除：数据权限、字段权限、功能权限、应用权限、角色绑定的用户。
        // 功能权限删除内部的用户缓存失效（含 user_field_perm）放各领域删除之后，兜住全部变更
        removeDataScopeByRoleIds(idList);
        removeFieldByRoleIds(idList);
        roleResourceRelService.removeByRoleIds(idList);
        roleAppRelService.removeByRoleIds(idList);
        userRoleRelService.removeByRoleIds(idList);

        return super.removeByIds(idList);
    }

    /**
     * 按角色删除数据权限授权，并按涉及的角色与菜单失效 RoleDataScope 缓存
     */
    private void removeDataScopeByRoleIds(Collection<? extends Serializable> roleIdList) {
        List<RoleDataScopeRel> relList = roleDataScopeRelMapper.selectListByQuery(QueryWrapper.create()
                .select(RoleDataScopeRel::getRoleId, RoleDataScopeRel::getMenuId)
                .where(RoleDataScopeRel::getRoleId).in(roleIdList));
        if (CollUtil.isEmpty(relList)) {
            return;
        }
        roleDataScopeRelMapper.deleteByQuery(QueryWrapper.create()
                .where(RoleDataScopeRel::getRoleId).in(roleIdList));
        cacheOps.del(relList.stream()
                .map(rel -> RoleDataScopeCacheKeyBuilder.build(rel.getRoleId(), rel.getMenuId()))
                .toList());
    }

    /**
     * 按角色删除字段受限关系（用户字段受限集缓存由功能权限删除统一失效）
     */
    private void removeFieldByRoleIds(Collection<? extends Serializable> roleIdList) {
        roleFieldRelMapper.deleteByQuery(QueryWrapper.create()
                .where(RoleFieldRel::getRoleId).in(roleIdList));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinTheRole(String code, Long userId) {
        Role role = getByCode(code);
        ArgumentAssert.notNull(role, "角色[{}]不存在", code);

        UserRoleRel userRoleRel = new UserRoleRel();
        userRoleRel.setRoleId(role.getId());
        userRoleRel.setUserId(userId);
        userRoleRelService.save(userRoleRel);
    }

    @Override
    @Transactional(readOnly = true)
    public Role getPermSetRoleOfCurrentOperator() {
        Integer nature = ContextUtil.getCurrentTopCompanyNature();
        ArgumentAssert.notNull(nature, "当前用户的组织性质未知，无法确定可分配范围");
        return getOne(QueryWrapper.create()
                .eq(Role::getRoleCategory, RoleCategoryEnum.PERM_SET.getCode())
                .eq(Role::getOrgNature, nature).eq(Role::getState, true));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataScopeEnum> getAssignableScopesOfMenu(Long menuId) {
        Role permSet = getPermSetRoleOfCurrentOperator();
        if (permSet == null) {
            return List.of();
        }
        RoleDataScopeRel rel = roleDataScopeRelMapper.selectOneByQuery(QueryWrapper.create()
                .eq(RoleDataScopeRel::getRoleId, permSet.getId())
                .eq(RoleDataScopeRel::getMenuId, menuId));
        return RoleService.getAssignableDataScopes(
                rel == null ? null : DataScopeEnum.getByCode(rel.getDataScope()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listAppIdsByRoleId(Long roleId) {
        return roleAppRelService.list(QueryWrapper.create()
                        .eq(RoleAppRel::getRoleId, roleId))
                .stream().map(RoleAppRel::getAppId).distinct().toList();
    }
}
