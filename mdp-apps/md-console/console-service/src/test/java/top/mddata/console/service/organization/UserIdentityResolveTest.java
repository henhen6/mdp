package top.mddata.console.service.organization;

import org.junit.jupiter.api.Test;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.organization.UserIdentityEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.entity.permission.Role;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 身份判定规则测试：规则见 docs/用户体系/用户角色组织权限体系设计.md 第 1 节。
 */
class UserIdentityResolveTest {

    private static Role role(String category, Integer nature) {
        // setter 由 RoleBase 生成且返回 RoleBase，无法在 Role 上链式调用，故用普通 set 写法
        Role role = new Role();
        role.setRoleCategory(category);
        role.setOrgNature(nature);
        return role;
    }

    @Test
    void 绑定运营管理员角色判定为运营者() {
        List<Role> roles = List.of(role(RoleCategoryEnum.ADMIN_ROLE.getCode(),
                OrgNatureEnum.OPERATIONS.getCode()));
        assertEquals(UserIdentityEnum.OPERATIONS_ADMIN, UserIdentityService.resolve(roles));
    }

    @Test
    void 绑定开发者管理员角色判定为开发者管理员() {
        List<Role> roles = List.of(role(RoleCategoryEnum.ADMIN_ROLE.getCode(),
                OrgNatureEnum.DEVELOPER.getCode()));
        assertEquals(UserIdentityEnum.DEVELOPER_ADMIN, UserIdentityService.resolve(roles));
    }

    @Test
    void 绑定开发者普通角色判定为开发者() {
        List<Role> roles = List.of(role(RoleCategoryEnum.NORMAL_ROLE.getCode(),
                OrgNatureEnum.DEVELOPER.getCode()));
        assertEquals(UserIdentityEnum.DEVELOPER, UserIdentityService.resolve(roles));
    }

    @Test
    void 只绑定总公司角色判定为普通用户() {
        List<Role> roles = List.of(role(RoleCategoryEnum.ADMIN_ROLE.getCode(),
                OrgNatureEnum.HEAD_COMPANY.getCode()));
        assertEquals(UserIdentityEnum.USER, UserIdentityService.resolve(roles));
    }

    @Test
    void 多角色时按最高身份判定() {
        List<Role> roles = List.of(
                role(RoleCategoryEnum.NORMAL_ROLE.getCode(),
                        OrgNatureEnum.HEAD_COMPANY.getCode()),
                role(RoleCategoryEnum.ADMIN_ROLE.getCode(),
                        OrgNatureEnum.OPERATIONS.getCode()));
        assertEquals(UserIdentityEnum.OPERATIONS_ADMIN, UserIdentityService.resolve(roles));
    }

    @Test
    void 无角色或空集合判定为普通用户() {
        assertEquals(UserIdentityEnum.USER, UserIdentityService.resolve(List.of()));
        assertEquals(UserIdentityEnum.USER, UserIdentityService.resolve(null));
    }

    @Test
    void 性质99普通角色判定为普通用户() {
        assertEquals(UserIdentityEnum.USER, UserIdentityService.resolve(List.of(
                role(RoleCategoryEnum.NORMAL_ROLE.getCode(), OrgNatureEnum.OPERATIONS.getCode()))));
    }
}
