package top.mddata.console.service.organization.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.constant.RoleCode;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.mapper.UserRoleRelMapper;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.mapper.permission.RoleMapper;
import top.mddata.console.service.organization.SystemProtectService;

import java.util.Collection;
import java.util.List;

/**
 * 系统硬保护 服务层实现。
 *
 * <p>保护规则见 docs/用户体系/用户角色组织权限体系设计.md 第 5 节：
 * 运营者（持有运营管理员角色的用户）、运营管理员角色本身、内置组织，
 * 在各写入口统一拦截，前端按钮隐藏仅体验优化，真正的保护在这里。</p>
 *
 * <p>本类只依赖 Mapper、不依赖任何 Service，从依赖方向上避免与角色域各服务形成循环依赖。
 * 运营者判定直接查启用状态（state=1）的 OPERATIONS_ADMIN 角色持有关系，
 * MyBatis-Flex 自动过滤逻辑删除数据（deleted_at），与 UserIdentityService 的判定
 * 在设计文档第 5 节数据契约下等价。</p>
 */
@Service
@RequiredArgsConstructor
public class SystemProtectServiceImpl implements SystemProtectService {
    private final UserRoleRelMapper userRoleRelMapper;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public boolean isOperationsAdminUser(Long userId) {
        if (userId == null) {
            return false;
        }
        return userRoleRelMapper.selectCountByQuery(operationsAdminHolderQuery()
                .and(UserRoleRel::getUserId).eq(userId)) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public void checkUsersNotProtected(Collection<Long> userIds, String action) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        List<UserRoleRel> hitList = userRoleRelMapper.selectListByQuery(
                operationsAdminHolderQuery()
                        .select(UserRoleRel::getUserId)
                        .and(UserRoleRel::getUserId).in(userIds));
        // 命中的第一个运营者即拒绝，错误消息携带具体用户id
        for (Long userId : hitList.stream().map(UserRoleRel::getUserId).distinct().toList()) {
            throw new ArgumentException("{}失败：用户[{}]是运营者，受系统保护", action, userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void checkRoleNotProtected(Long roleId, String action) {
        if (roleId == null) {
            return;
        }
        Role role = roleMapper.selectOneByQuery(QueryWrapper.create().eq(Role::getId, roleId));
        // 运营管理员及其权限集合角色是运营体系的根基，删除会导致运营者权限失效
        boolean protectedRole = role != null
                && (RoleCode.OPERATIONS_ADMIN.equals(role.getCode())
                || RoleCode.OPERATIONS_ADMIN_COLL.equals(role.getCode()));
        ArgumentAssert.isFalse(protectedRole, "{}失败：角色[{}]受系统保护",
                action, protectedRole ? role.getName() : null);
    }

    @Override
    public void checkOrgNotBuiltIn(Collection<Long> orgIds, String action) {
        if (CollUtil.isEmpty(orgIds)) {
            return;
        }
        orgIds.forEach(orgId -> ArgumentAssert.isFalse(SystemProtectService.isBuiltInOrg(orgId),
                "{}失败：该组织是系统内置组织，受系统保护", action));
    }

    /**
     * 构造“启用状态运营管理员角色持有关系”基础查询：用户角色关联内联启用的运营管理员角色。
     */
    private QueryWrapper operationsAdminHolderQuery() {
        return QueryWrapper.create()
                .from(UserRoleRel.class)
                .innerJoin(Role.class).on(UserRoleRel::getRoleId, Role::getId)
                .where(Role::getCode).eq(RoleCode.OPERATIONS_ADMIN)
                .and(Role::getState).eq(true);
    }
}
