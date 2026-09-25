package top.mddata.console.service.organization.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.UserOrgRel;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.mapper.UserRoleRelMapper;
import top.mddata.console.dto.organization.UserRoleRelDto;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.mapper.permission.RoleMapper;
import top.mddata.console.service.organization.OrgService;
import top.mddata.console.service.organization.SystemProtectService;
import top.mddata.console.service.organization.UserOrgRelService;
import top.mddata.console.service.organization.UserRoleRelService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色关联 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserRoleRelServiceImpl extends SuperServiceImpl<UserRoleRelMapper, UserRoleRel> implements UserRoleRelService {

    private final SystemProtectService systemProtectService;
    private final RoleMapper roleMapper;
    private final UserOrgRelService userOrgRelService;
    private final OrgService orgService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRoleIds(Collection<? extends Serializable> roleIdList) {
        if (CollUtil.isEmpty(roleIdList)) {
            return;
        }
        super.remove(QueryWrapper.create().in(UserRoleRel::getRoleId, roleIdList));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveByDto(UserRoleRelDto dto) {
        checkRoleUserNatureMatch(dto.getRoleId(), dto.getUserIdList());

        List<UserRoleRel> existList = list(QueryWrapper.create().eq(UserRoleRel::getRoleId, dto.getRoleId()));
        List<Long> existUserIdList = existList.stream().map(UserRoleRel::getUserId).distinct().toList();

        List<UserRoleRel> saveList = dto.getUserIdList().stream()
                // 已存在的不添加
                .filter(userId -> !existUserIdList.contains(userId))
                .map(userId -> {
                    UserRoleRel rel = new UserRoleRel();
                    rel.setRoleId(dto.getRoleId());
                    rel.setUserId(userId);
                    return rel;
                })
                .collect(Collectors.toList());

        return super.saveBatch(saveList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(UserRoleRelDto dto) {
        systemProtectService.checkUsersNotProtected(dto.getUserIdList(), "解绑角色");

        return super.remove(QueryWrapper.create().eq(UserRoleRel::getRoleId, dto.getRoleId()).in(UserRoleRel::getUserId, dto.getUserIdList()));

    }

    @Override
    @Transactional(readOnly = true)
    public void checkRoleUserNatureMatch(Long roleId, Collection<Long> userIds) {
        ArgumentAssert.notNull(roleId, "角色id不能为空");
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        Role role = roleMapper.selectOneByQuery(QueryWrapper.create().eq(Role::getId, roleId));
        ArgumentAssert.notNull(role, "角色[{}]不存在", roleId);
        ArgumentAssert.notNull(role.getOrgNature(), "角色[{}]未配置组织性质，无法执行绑定校验",
                role.getName());

        for (Long userId : userIds) {
            List<UserOrgRel> relList = userOrgRelService.list(
                    QueryWrapper.create().eq(UserOrgRel::getUserId, userId));
            ArgumentAssert.isFalse(CollUtil.isEmpty(relList),
                    "绑定失败：用户[{}]不属于任何组织，无法判定其身份", userId);
            for (UserOrgRel rel : relList) {
                Org org = orgService.getByIdCache(rel.getOrgId());
                ArgumentAssert.isTrue(org != null && role.getOrgNature().equals(org.getNature()),
                        "绑定失败：用户[{}]所属组织性质与角色[{}]的组织性质不一致", userId, role.getName());
            }
        }
    }
}
