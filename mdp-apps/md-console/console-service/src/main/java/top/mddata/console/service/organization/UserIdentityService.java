package top.mddata.console.service.organization;

import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.organization.UserIdentityEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.entity.permission.Role;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用户身份 服务层。
 *
 * <p>身份由用户绑定的角色决定（docs/用户体系/用户角色组织权限体系设计.md 第 1 节）。</p>
 */
public interface UserIdentityService {

    /**
     * 判定用户身份。
     * 仅启用状态的角色参与身份判定，禁用的管理员角色会导致身份降级。
     *
     * @param userId 用户id
     * @return 用户身份，用户不存在或无角色时返回 普通用户
     */
    UserIdentityEnum getIdentity(Long userId);

    /**
     * 批量判定用户身份（列表页用，避免 N+1）。
     * 仅启用状态的角色参与身份判定，禁用的管理员角色会导致身份降级。
     *
     * @param userIds 用户id集合
     * @return key=用户id，value=身份；入参中的每个 id 都会有值
     */
    Map<Long, UserIdentityEnum> mapIdentity(Collection<Long> userIds);

    /**
     * 身份判定规则（纯函数）：
     * 有性质99管理员角色 → 运营者；有性质90管理员角色 → 开发者管理员；
     * 有性质90其他角色 → 开发者；其余 → 普通用户。
     *
     * @param roles 用户绑定的角色集合
     * @return 用户身份
     */
    static UserIdentityEnum resolve(Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return UserIdentityEnum.USER;
        }
        List<Role> validRoles = roles.stream().filter(Objects::nonNull).toList();
        boolean hasOperationsAdmin = validRoles.stream().anyMatch(role ->
                RoleCategoryEnum.ADMIN_ROLE.getCode().equals(role.getRoleCategory())
                        && OrgNatureEnum.OPERATIONS.eq(role.getOrgNature()));
        if (hasOperationsAdmin) {
            return UserIdentityEnum.OPERATIONS_ADMIN;
        }
        boolean hasDeveloperAdmin = validRoles.stream().anyMatch(role ->
                RoleCategoryEnum.ADMIN_ROLE.getCode().equals(role.getRoleCategory())
                        && OrgNatureEnum.DEVELOPER.eq(role.getOrgNature()));
        if (hasDeveloperAdmin) {
            return UserIdentityEnum.DEVELOPER_ADMIN;
        }
        boolean hasDeveloper = validRoles.stream()
                .anyMatch(role -> OrgNatureEnum.DEVELOPER.eq(role.getOrgNature()));
        return hasDeveloper ? UserIdentityEnum.DEVELOPER : UserIdentityEnum.USER;
    }

    /**
     * 校验注册身份是否合法：仅允许 1-总公司（普通用户）、90-开发者。
     * 运营身份不开放注册。
     *
     * @param nature 注册入参的组织性质
     * @return true=允许注册
     */
    static boolean checkRegisterNature(Integer nature) {
        return OrgNatureEnum.HEAD_COMPANY.eq(nature) || OrgNatureEnum.DEVELOPER.eq(nature);
    }
}
