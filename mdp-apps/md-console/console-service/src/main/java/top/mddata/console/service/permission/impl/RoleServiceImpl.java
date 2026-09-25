package top.mddata.console.service.permission.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.mybatisflex.datapermission.DataScope;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.constant.RoleCode;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.entity.permission.RoleAppRel;
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
        // 数据范围为空视同全部（防止 null 落库绕过 DDL 默认值与"空视同 ALL"契约）
        if (StrUtil.isEmpty(entity.getDataScope())) {
            entity.setDataScope(DataScope.ALL.getCode());
        }
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
        ArgumentAssert.notNull(nature,
                "当前用户的组织性质未知，无法确定可分配范围");
        return getOne(QueryWrapper.create()
                .eq(Role::getRoleCategory, RoleCategoryEnum.PERM_SET.getCode())
                .eq(Role::getOrgNature, nature).eq(Role::getState, true));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataScope> getAssignableDataScopes() {
        Role permSet = getPermSetRoleOfCurrentOperator();
        if (permSet == null) {
            // 权限集合角色不存在=配置缺失，无可分配项（fail-closed）
            return List.of();
        }
        DataScope permSetScope = DataScope.getByCode(permSet.getDataScope());
        return RoleService.getAssignableDataScopes(permSetScope);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listAppIdsByRoleId(Long roleId) {
        return roleAppRelService.list(QueryWrapper.create()
                        .eq(RoleAppRel::getRoleId, roleId))
                .stream().map(RoleAppRel::getAppId).distinct().toList();
    }
}
