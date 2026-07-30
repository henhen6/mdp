package top.mddata.console.service.permission.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.mapper.permission.RoleMapper;
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
}
