package top.mddata.console.service.organization.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.enumeration.organization.UserIdentityEnum;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.service.organization.UserIdentityService;
import top.mddata.console.service.organization.UserRoleRelService;
import top.mddata.console.service.permission.RoleService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户身份 服务层实现。
 */
@Service
@RequiredArgsConstructor
public class UserIdentityServiceImpl implements UserIdentityService {
    private final RoleService roleService;
    private final UserRoleRelService userRoleRelService;

    @Override
    @Transactional(readOnly = true)
    public UserIdentityEnum getIdentity(Long userId) {
        if (userId == null) {
            return UserIdentityEnum.USER;
        }
        return UserIdentityService.resolve(listUserRoles(List.of(userId))
                .getOrDefault(userId, Collections.emptyList()));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, UserIdentityEnum> mapIdentity(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, List<Role>> roleMap = listUserRoles(userIds.stream().distinct().toList());
        // 入参中的每个 id 都必须有值，无角色的按普通用户处理
        return userIds.stream().collect(Collectors.toMap(Function.identity(),
                userId -> UserIdentityService.resolve(
                        roleMap.getOrDefault(userId, Collections.emptyList())),
                (a, b) -> a));
    }

    /**
     * 按用户id分组查询其绑定的启用角色
     */
    private Map<Long, List<Role>> listUserRoles(List<Long> userIds) {
        // 1. 查关系：userId -> roleId
        QueryWrapper relQuery = QueryWrapper.create().select().from(UserRoleRel.class)
                .where(UserRoleRel::getUserId).in(userIds);
        List<UserRoleRel> relList = userRoleRelService.list(relQuery);
        if (relList.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2. 查启用状态的角色
        List<Long> roleIds = relList.stream().map(UserRoleRel::getRoleId).distinct().toList();
        List<Role> roles = roleService.list(QueryWrapper.create()
                .where(Role::getId).in(roleIds).and(Role::getState).eq(true));
        Map<Long, Role> roleById = roles.stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));

        // 3. 按 userId 分组
        Map<Long, List<Role>> result = new HashMap<>();
        for (UserRoleRel rel : relList) {
            Role role = roleById.get(rel.getRoleId());
            if (role != null) {
                result.computeIfAbsent(rel.getUserId(), k -> new ArrayList<>()).add(role);
            }
        }
        return result;
    }
}
