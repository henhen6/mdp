package top.mddata.common.constant;

import java.util.List;

/**
 * 系统内置组织ID。
 *
 * <p>取值与 docs/mdp.sql 中的内置数据一一对应，
 * 属于跨环境契约，禁止修改。</p>
 */
public interface BuiltInOrgId {
    /**
     * 运营中心（组织性质=99 运营）
     */
    long OPERATIONS_CENTER = 687106879444787201L;

    /**
     * 开发者平台（组织性质=90 开发者）
     */
    long DEVELOPER_PLATFORM = 687106879444787202L;

    /**
     * 总公司（组织性质=1 总公司）
     */
    long HEAD_COMPANY = 687106879444787203L;

    /**
     * 总公司下的默认部门（普通用户注册落入）
     */
    long DEFAULT_DEPT = 687106879444787204L;

    /**
     * 所有内置组织ID
     */
    List<Long> ALL = List.of(OPERATIONS_CENTER, DEVELOPER_PLATFORM, HEAD_COMPANY, DEFAULT_DEPT);
}
