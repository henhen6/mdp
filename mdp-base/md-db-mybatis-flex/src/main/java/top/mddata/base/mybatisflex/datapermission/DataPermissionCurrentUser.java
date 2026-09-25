package top.mddata.base.mybatisflex.datapermission;

import java.util.Set;

/**
 * 当前用户信息
 *
 * @author henhen
 * @since 2026年05月24日
 */
public class DataPermissionCurrentUser {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 角色列表
     */
    private Set<CurrentUserRole> roles;

    /**
     * 部门 ID
     */
    private Long deptId;

    /**
     * 公司 ID
     */
    private Long companyId;

    /**
     * 当前用户角色信息
     */
    public static class CurrentUserRole {

        /**
         * 角色 ID
         */
        private Long roleId;

        /**
         * 数据权限
         */
        private DataScope dataScope;

        /**
         * 自定义数据范围实现类（Spring Bean 名），仅 dataScope=CUSTOM 时有值
         */
        private String dataScopeImpl;

        public CurrentUserRole() {
        }

        public CurrentUserRole(Long roleId, DataScope dataScope) {
            this(roleId, dataScope, null);
        }

        public CurrentUserRole(Long roleId, DataScope dataScope, String dataScopeImpl) {
            this.roleId = roleId;
            this.dataScope = dataScope;
            this.dataScopeImpl = dataScopeImpl;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public DataScope getDataScope() {
            return dataScope;
        }

        public void setDataScope(DataScope dataScope) {
            this.dataScope = dataScope;
        }

        public String getDataScopeImpl() {
            return dataScopeImpl;
        }

        public void setDataScopeImpl(String dataScopeImpl) {
            this.dataScopeImpl = dataScopeImpl;
        }
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Set<CurrentUserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<CurrentUserRole> roles) {
        this.roles = roles;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
