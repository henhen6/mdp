package top.mddata.workbench.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.User;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.facade.message.MsgFacade;
import top.mddata.console.facade.organization.UserFacade;
import top.mddata.workbench.service.SsoUserService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link AuthServiceImpl#findOrg(User)} 组织上下文解析单测。
 *
 * <p>回归背景：原实现在"上次单位/顶级单位不在用户直属机构集合"时直接清空且本次不重算，
 * 导致只挂部门的用户（直属集合不含公司）每次 SSO 换 token 时公司参数被误清。</p>
 *
 * @author henhen6
 * @since 2026-10-09
 */
class AuthServiceImplFindOrgTest {

    private static final Long USER_ID = 1L;
    private static final Long COMPANY_ID = 100L;
    private static final Long DEPT_ID = 201L;
    private static final Long NEW_DEPT_ID = 202L;
    private static final Integer COMPANY_NATURE = 10;

    private SsoUserService ssoUserService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ssoUserService = mock(SsoUserService.class);
        authService = new AuthServiceImpl(mock(SystemProperties.class), mock(cn.dev33.satoken.config.SaTokenConfig.class),
                mock(CacheOps.class), ssoUserService, mock(UserFacade.class), mock(MsgFacade.class), Map.of());
    }

    private Org company(Long id, String treePath) {
        Org org = new Org();
        org.setId(id);
        org.setOrgType("10");
        org.setTreePath(treePath);
        org.setNature(COMPANY_NATURE);
        return org;
    }

    private Org dept(Long id, String treePath) {
        Org org = new Org();
        org.setId(id);
        org.setOrgType("20");
        org.setTreePath(treePath);
        return org;
    }

    private User user(Long lastDeptId, Long lastCompanyId, Long lastTopCompanyId) {
        User user = new User();
        user.setId(USER_ID);
        user.setLastDeptId(lastDeptId);
        user.setLastCompanyId(lastCompanyId);
        user.setLastTopCompanyId(lastTopCompanyId);
        return user;
    }

    /**
     * 核心回归：用户只挂部门（直属集合不含公司），上次公司仍有效时必须保留，
     * 不能被"直属集合校验"误判失效而清空。
     */
    @Test
    void 挂部门用户_上次公司不在直属集合时应保留而非清空() {
        Org company = company(COMPANY_ID, "/100/");
        when(ssoUserService.findOrgIdByUserId(USER_ID)).thenReturn(List.of(DEPT_ID));
        when(ssoUserService.findCompanyByUserId(USER_ID)).thenReturn(List.of(company));
        when(ssoUserService.getOrgByIdCache(COMPANY_ID)).thenReturn(company);
        when(ssoUserService.getOrgNatureByOrgId(COMPANY_ID)).thenReturn(COMPANY_NATURE);

        AuthServiceImpl.TempOrg org = authService.findOrg(user(DEPT_ID, COMPANY_ID, COMPANY_ID));

        assertEquals(DEPT_ID, org.getCurrentDeptId());
        assertEquals(COMPANY_ID, org.getCurrentCompanyId());
        assertEquals(COMPANY_ID, org.getCurrentTopCompanyId());
        assertEquals(COMPANY_NATURE, org.getCurrentCompanyNature());
        assertEquals(COMPANY_NATURE, org.getCurrentTopCompanyNature());
    }

    /**
     * 上次部门已被移出用户组织时，应当场重选一个部门（而非置 null 放弃本次解析）。
     */
    @Test
    void 上次部门被移除后应重选新部门() {
        Org newDept = dept(NEW_DEPT_ID, "/100/202/");
        Org company = company(COMPANY_ID, "/100/");
        when(ssoUserService.findOrgIdByUserId(USER_ID)).thenReturn(List.of(NEW_DEPT_ID));
        when(ssoUserService.findDeptByUserId(USER_ID, null)).thenReturn(List.of(newDept));
        when(ssoUserService.getDefaultOrg(any(), isNull())).thenReturn(newDept);
        when(ssoUserService.findCompanyByUserId(USER_ID)).thenReturn(List.of(company));
        when(ssoUserService.getCompanyByDeptId(NEW_DEPT_ID)).thenReturn(company);
        when(ssoUserService.getOrgByIdCache(COMPANY_ID)).thenReturn(company);
        when(ssoUserService.getOrgNatureByOrgId(COMPANY_ID)).thenReturn(COMPANY_NATURE);

        AuthServiceImpl.TempOrg org = authService.findOrg(user(299L, null, null));

        assertEquals(NEW_DEPT_ID, org.getCurrentDeptId());
        assertEquals(COMPANY_ID, org.getCurrentCompanyId());
        assertEquals(COMPANY_ID, org.getCurrentTopCompanyId());
    }

    /**
     * 用户不属于任何组织时，全部字段为 null（fillTokenSession 将据此清理 session 参数）。
     */
    @Test
    void 无组织用户返回全空() {
        when(ssoUserService.findOrgIdByUserId(USER_ID)).thenReturn(List.of());
        when(ssoUserService.findDeptByUserId(USER_ID, null)).thenReturn(List.of());
        when(ssoUserService.getDefaultOrg(any(), isNull())).thenReturn(null);
        when(ssoUserService.findCompanyByUserId(USER_ID)).thenReturn(List.of());

        AuthServiceImpl.TempOrg org = authService.findOrg(user(null, null, null));

        assertNull(org.getCurrentDeptId());
        assertNull(org.getCurrentCompanyId());
        assertNull(org.getCurrentCompanyNature());
        assertNull(org.getCurrentTopCompanyId());
        assertNull(org.getCurrentTopCompanyNature());
    }

    /**
     * 用户直接挂公司节点（无部门）：公司来自单位列表，顶级公司由 treePath 推导。
     */
    @Test
    void 直接挂公司用户解析公司与顶级公司() {
        Org company = company(COMPANY_ID, "/100/");
        when(ssoUserService.findOrgIdByUserId(USER_ID)).thenReturn(List.of(COMPANY_ID));
        when(ssoUserService.findDeptByUserId(USER_ID, null)).thenReturn(List.of());
        when(ssoUserService.getDefaultOrg(any(), isNull())).thenAnswer(inv -> {
            List<Org> list = inv.getArgument(0);
            return list.isEmpty() ? null : list.get(0);
        });
        when(ssoUserService.findCompanyByUserId(USER_ID)).thenReturn(List.of(company));
        when(ssoUserService.getOrgByIdCache(COMPANY_ID)).thenReturn(company);
        when(ssoUserService.getOrgNatureByOrgId(COMPANY_ID)).thenReturn(COMPANY_NATURE);

        AuthServiceImpl.TempOrg org = authService.findOrg(user(null, null, null));

        assertNull(org.getCurrentDeptId());
        assertEquals(COMPANY_ID, org.getCurrentCompanyId());
        assertEquals(COMPANY_ID, org.getCurrentTopCompanyId());
    }

    /**
     * 顶级公司必须始终与当前公司同源：上次顶级公司残留脏值时，按当前公司 treePath 重推导。
     */
    @Test
    void 上次顶级公司残留脏值时按当前公司重推导() {
        Org company = company(300L, "/100/300/");
        Org topCompany = company(COMPANY_ID, "/100/");
        when(ssoUserService.findOrgIdByUserId(USER_ID)).thenReturn(List.of(DEPT_ID));
        when(ssoUserService.findCompanyByUserId(USER_ID)).thenReturn(List.of(company));
        when(ssoUserService.getOrgByIdCache(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return COMPANY_ID.equals(id) ? topCompany : company;
        });
        when(ssoUserService.getOrgNatureByOrgId(anyLong())).thenReturn(COMPANY_NATURE);

        // lastTopCompanyId=999 为脏数据（不存在于任何组织），lastCompanyId=300 有效
        AuthServiceImpl.TempOrg org = authService.findOrg(user(DEPT_ID, 300L, 999L));

        assertEquals(300L, org.getCurrentCompanyId());
        assertEquals(COMPANY_ID, org.getCurrentTopCompanyId());
    }
}
