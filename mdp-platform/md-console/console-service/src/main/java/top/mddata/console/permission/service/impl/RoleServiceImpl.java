package top.mddata.console.permission.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Multimap;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.permission.entity.Role;
import top.mddata.console.permission.mapper.RoleMapper;
import top.mddata.console.permission.service.RoleService;

import java.util.Collection;
import java.util.Map;

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
    @Override
    @Transactional(readOnly = true)
    public Boolean checkCode(String code, Long id) {
        if (StrUtil.isEmptyIfStr(code)) {
            return true;
        }
        return mapper.selectCountByQuery(QueryWrapper.create().eq(Role::getCode, code).ne(Role::getId, id)) > 0;
    }

    @Override
    protected Role saveBefore(Object save) {
        Role entity = BeanUtil.toBean(save, getEntityClass());
        entity.setId(null);
        entity.setOrgNature(OrgNatureEnum.DEFAULT.getCode());
        entity.setTemplateRole(false);
        entity.setRoleCategory(RoleCategoryEnum.NORMAL_ROLE.getCode());
        ArgumentAssert.isFalse(checkCode(entity.getCode(), null), "角色编码重复");
        return entity;
    }

    @Override
    protected Role updateBefore(Object updateDto) {
        Role entity = BeanUtil.toBean(updateDto, getEntityClass());
        entity.setOrgNature(OrgNatureEnum.DEFAULT.getCode());
        entity.setTemplateRole(false);
        entity.setRoleCategory(RoleCategoryEnum.NORMAL_ROLE.getCode());
        ArgumentAssert.isFalse(checkCode(entity.getCode(), entity.getId()), "角色编码重复");
        return entity;
    }
}
