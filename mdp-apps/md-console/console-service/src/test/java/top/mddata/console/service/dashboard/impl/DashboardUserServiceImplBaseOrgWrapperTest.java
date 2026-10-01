package top.mddata.console.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import top.mddata.common.entity.Org;
import top.mddata.common.enumeration.organization.OrgTypeEnum;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DashboardUserServiceImpl.baseOrgWrapper 生成 SQL 的结构校验：
 * 基础条件 + orgType 必须落在 mdc_org 表上，不得出现 query_wrapper 幻影表。
 */
class DashboardUserServiceImplBaseOrgWrapperTest {

    @Test
    void 限定范围时SQL应包含全部条件且表为Org() {
        DashboardUserServiceImpl.OrgScope scope =
                new DashboardUserServiceImpl.OrgScope(false, 1, 100L, "/100/");

        QueryWrapper wrapper = DashboardUserServiceImpl.baseOrgWrapper(scope)
                .eq(Org::getOrgType, OrgTypeEnum.COMPANY.getCode());
        String sql = wrapper.toSQL();

        assertTrue(sql.contains("FROM `mdc_org`"), "FROM 必须是 mdc_org 表: " + sql);
        assertFalse(sql.contains("query_wrapper"), "不得出现 query_wrapper 幻影表: " + sql);
        assertTrue(sql.contains("`state` = true"), "缺少 state 条件: " + sql);
        assertTrue(sql.contains("`deleted_at` = 0"), "缺少 deleted_at 条件: " + sql);
        assertTrue(sql.contains("`tree_path` LIKE '%/100/%'"), "缺少 tree_path 条件: " + sql);
        assertTrue(sql.contains("`org_type` = '10'"), "缺少 org_type 条件: " + sql);
    }

    @Test
    void 全范围时SQL不含treePath条件() {
        DashboardUserServiceImpl.OrgScope scope =
                new DashboardUserServiceImpl.OrgScope(true, 1, null, null);

        String sql = DashboardUserServiceImpl.baseOrgWrapper(scope)
                .eq(Org::getOrgType, OrgTypeEnum.DEPT.getCode()).toSQL();

        assertTrue(sql.contains("FROM `mdc_org`"), "FROM 必须是 mdc_org 表: " + sql);
        assertFalse(sql.contains("tree_path"), "全范围不应有 tree_path 条件: " + sql);
    }
}
