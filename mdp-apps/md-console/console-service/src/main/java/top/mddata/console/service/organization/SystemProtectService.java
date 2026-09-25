package top.mddata.console.service.organization;

import top.mddata.common.constant.BuiltInOrgId;

import java.util.Collection;

/**
 * 系统硬保护 服务层。
 *
 * <p>保护规则见 docs/用户体系/用户角色组织权限体系设计.md 第 5 节：
 * 运营者（持有运营管理员角色的用户）、运营管理员角色本身、内置组织，
 * 在各写入口统一拦截，前端按钮隐藏仅体验优化，真正的保护在这里。</p>
 */
public interface SystemProtectService {

    /**
     * 用户是否运营者（持有启用状态的运营管理员角色）
     *
     * @param userId 用户id
     * @return true=运营者
     */
    boolean isOperationsAdminUser(Long userId);

    /**
     * 校验用户列表不含运营者，含则 fail fast
     *
     * @param userIds 待操作用户id
     * @param action  操作描述（用于错误消息，如"删除用户"）
     */
    void checkUsersNotProtected(Collection<Long> userIds, String action);

    /**
     * 校验角色不是运营管理员角色，是则 fail fast
     *
     * @param roleId 角色id
     * @param action 操作描述
     */
    void checkRoleNotProtected(Long roleId, String action);

    /**
     * 校验组织列表不含内置组织，含则 fail fast
     *
     * @param orgIds 待操作组织id
     * @param action 操作描述
     */
    void checkOrgNotBuiltIn(Collection<Long> orgIds, String action);

    /**
     * 是否系统内置组织（3 根公司 + 默认部门）
     *
     * @param orgId 组织id
     * @return true=内置组织
     */
    static boolean isBuiltInOrg(Long orgId) {
        if (orgId == null) {
            return false;
        }
        return BuiltInOrgId.ALL.contains(orgId);
    }
}
