package top.mddata.common.constant;

import java.util.Set;

/**
 * 系统内置角色编码。
 *
 * <p>取值与 docs/mdp.sql 中 mdc_role 的 code 一一对应，
 * 属于跨环境契约，禁止修改。</p>
 */
public interface RoleCode {
    /**
     * [管理员角色] 运营管理员：系统最高权限，受硬保护
     */
    String OPERATIONS_ADMIN = "OPERATIONS_ADMIN";

    /**
     * [权限集合] 运营管理员权限集
     */
    String OPERATIONS_ADMIN_COLL = "OPERATIONS_ADMIN_COLL";

    /**
     * [管理员角色] 总公司管理员
     */
    String ADMIN = "ADMIN";

    /**
     * [权限集合] 总公司管理员权限集
     */
    String ADMIN_COLL = "ADMIN_COLL";

    /**
     * [管理员角色] 开发者管理员
     */
    String DEVELOPER_ADMIN = "DEVELOPER_ADMIN";

    /**
     * [权限集合] 开发者管理员权限集
     */
    String DEVELOPER_ADMIN_COLL = "DEVELOPER_ADMIN_COLL";

    /**
     * [普通角色] 开发者：新注册开发者默认绑定
     */
    String DEFAULT_DEVELOPER = "DEFAULT_DEVELOPER";

    /**
     * [普通角色] 普通用户：新注册用户默认绑定
     */
    String DEFAULT_USER = "DEFAULT_USER";

    /**
     * 系统内置保留编码（普通角色禁止使用）
     */
    Set<String> BUILT_IN_CODES = Set.of(OPERATIONS_ADMIN, OPERATIONS_ADMIN_COLL, ADMIN, ADMIN_COLL,
            DEVELOPER_ADMIN, DEVELOPER_ADMIN_COLL, DEFAULT_DEVELOPER, DEFAULT_USER);
}
