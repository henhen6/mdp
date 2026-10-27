package top.mddata.console.service.organization.impl;

import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.exception.ArgumentException;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.UserOrgRel;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.common.mapper.UserRoleRelMapper;
import top.mddata.console.dto.organization.UserRoleSaveDto;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.mapper.permission.RoleMapper;
import top.mddata.console.service.organization.OrgService;
import top.mddata.console.service.organization.SystemProtectService;
import top.mddata.console.service.organization.UserOrgRelService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 用户侧分配角色：全量覆盖保存，只覆盖可分配范围（非模板 + 普通角色）内的关联。
 *
 * @author henhen6
 * @since 2026-10-27
 */
class UserRoleRelServiceImplAssignRoleTest {

    private static final Long USER_ID = 100L;
    private static final Long ORG_ID = 9L;

    private SystemProtectService systemProtectService;
    private RoleMapper roleMapper;
    private UserOrgRelService userOrgRelService;
    private OrgService orgService;
    private UserRoleRelMapper mapper;
    private CacheOps cacheOps;
    private UserRoleRelServiceImpl service;

    @BeforeEach
    void setUp() {
        systemProtectService = mock(SystemProtectService.class);
        roleMapper = mock(RoleMapper.class);
        userOrgRelService = mock(UserOrgRelService.class);
        orgService = mock(OrgService.class);
        mapper = mock(UserRoleRelMapper.class);
        cacheOps = mock(CacheOps.class);
        service = new UserRoleRelServiceImpl(systemProtectService, roleMapper, userOrgRelService, orgService);
        // mapper 由 mybatis-flex ServiceImpl 以 @Autowired 字段注入，测试中反射替换
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);
    }

    private static Role role(Long id, boolean template, Integer nature) {
        // setter 由 RoleBase 生成且返回 RoleBase，无法在 Role 上链式调用，故用普通 set 写法
        Role role = new Role();
        role.setId(id);
        role.setName("角色" + id);
        role.setRoleCategory(RoleCategoryEnum.NORMAL_ROLE.getCode());
        role.setTemplateRole(template);
        role.setOrgNature(nature);
        return role;
    }

    private static UserRoleRel rel(Long roleId) {
        UserRoleRel rel = new UserRoleRel();
        rel.setUserId(USER_ID);
        rel.setRoleId(roleId);
        return rel;
    }

    private static UserRoleSaveDto dto(List<Long> roleIdList) {
        return UserRoleSaveDto.builder().userId(USER_ID).roleIdList(roleIdList).build();
    }

    /** 断言删除 SQL 只覆盖角色1：角色条件取值为1，不含模板角色2 */
    private static void assertDeleteSqlOnlyRole1(QueryWrapper deleteWrapper) {
        String sql = deleteWrapper.toSQL().replaceAll("\\s+", " ").toLowerCase();
        assertTrue(sql.contains("role_id"), "删除条件应包含角色ID列，实际SQL：" + sql);
        // 单元素 IN 会被 mybatis-flex 归并为等值条件，两种写法都兼容
        assertTrue(sql.contains("= 1") || sql.contains("in (1)"),
                "删除范围应只包含角色1，实际SQL：" + sql);
        assertFalse(sql.contains("2"), "模板角色2不应在删除范围内，实际SQL：" + sql);
    }

    /** 用户挂在"总公司"性质的组织下 */
    private void mockUserOrg() {
        UserOrgRel userOrgRel = new UserOrgRel();
        userOrgRel.setUserId(USER_ID);
        userOrgRel.setOrgId(ORG_ID);
        when(userOrgRelService.list(any(QueryWrapper.class))).thenReturn(List.of(userOrgRel));
        Org org = new Org();
        org.setNature(OrgNatureEnum.HEAD_COMPANY.getCode());
        when(orgService.getByIdCache(ORG_ID)).thenReturn(org);
    }

    @Test
    void 保存_角色性质与用户组织性质不匹配_failFast() {
        mockUserOrg();
        when(roleMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(role(1L, false, OrgNatureEnum.OPERATIONS.getCode())));

        assertThrows(ArgumentException.class, () -> service.saveUserRole(dto(List.of(1L))));
        verify(mapper, never()).insert(any(UserRoleRel.class));
        verify(mapper, never()).deleteByQuery(any(QueryWrapper.class));
    }

    @Test
    void 保存_提交可分配范围外角色_拒绝() {
        // 模板角色被可分配范围过滤，查询结果为空，与提交数量不一致即拒绝
        when(roleMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());

        assertThrows(ArgumentException.class, () -> service.saveUserRole(dto(List.of(2L))));
        verifyNoInteractions(userOrgRelService);
    }

    @Test
    void 保存_用户不属于任何组织_failFast() {
        when(roleMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(role(1L, false, OrgNatureEnum.HEAD_COMPANY.getCode())));
        when(userOrgRelService.list(any(QueryWrapper.class))).thenReturn(List.of());

        assertThrows(ArgumentException.class, () -> service.saveUserRole(dto(List.of(1L))));
        verify(mapper, never()).insert(any(UserRoleRel.class));
    }

    @Test
    void 保存_用户挂在两个不同性质组织_任何角色都拒绝() {
        // ALL 语义：角色性质须等于用户每个组织的性质，混合性质组织下无可分配角色
        UserOrgRel rel1 = new UserOrgRel();
        rel1.setUserId(USER_ID);
        rel1.setOrgId(ORG_ID);
        UserOrgRel rel2 = new UserOrgRel();
        rel2.setUserId(USER_ID);
        rel2.setOrgId(10L);
        when(userOrgRelService.list(any(QueryWrapper.class))).thenReturn(List.of(rel1, rel2));
        Org org1 = new Org();
        org1.setNature(OrgNatureEnum.HEAD_COMPANY.getCode());
        Org org2 = new Org();
        org2.setNature(OrgNatureEnum.OPERATIONS.getCode());
        when(orgService.getByIdCache(ORG_ID)).thenReturn(org1);
        when(orgService.getByIdCache(10L)).thenReturn(org2);
        when(roleMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(role(1L, false, OrgNatureEnum.HEAD_COMPANY.getCode())));

        assertThrows(ArgumentException.class, () -> service.saveUserRole(dto(List.of(1L))));
        verify(mapper, never()).insert(any(UserRoleRel.class));
    }

    @Test
    void 保存_空集合_只删除可分配范围内旧关联() {
        when(mapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(rel(1L), rel(2L)));
        // role 2 是模板角色，被可分配范围过滤，不在删除范围内
        when(roleMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(role(1L, false, OrgNatureEnum.HEAD_COMPANY.getCode())));

        assertTrue(service.saveUserRole(dto(List.of())));

        verify(systemProtectService).checkUsersNotProtected(List.of(USER_ID), "分配角色");
        ArgumentCaptor<QueryWrapper> deleteCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).deleteByQuery(deleteCaptor.capture());
        // 模板角色2必须排除在删除范围外
        assertDeleteSqlOnlyRole1(deleteCaptor.getValue());
        verify(mapper, never()).insert(any(UserRoleRel.class));
        verify(cacheOps).del(anyCollection());
        // 空集合不触发组织性质校验
        verifyNoInteractions(userOrgRelService);
    }

    @Test
    void 保存_正常_删旧插新并失效缓存() {
        mockUserOrg();
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(rel(1L)));
        // 第一次查询：校验提交的角色；第二次查询：过滤待删除的旧角色
        when(roleMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(role(3L, false, OrgNatureEnum.HEAD_COMPANY.getCode())))
                .thenReturn(List.of(role(1L, false, OrgNatureEnum.HEAD_COMPANY.getCode())));

        assertTrue(service.saveUserRole(dto(List.of(3L))));

        ArgumentCaptor<QueryWrapper> deleteCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).deleteByQuery(deleteCaptor.capture());
        // 旧关联只含角色1，删除范围不得扩大
        assertDeleteSqlOnlyRole1(deleteCaptor.getValue());
        ArgumentCaptor<UserRoleRel> captor = ArgumentCaptor.forClass(UserRoleRel.class);
        verify(mapper).insert(captor.capture());
        assertEquals(3L, captor.getValue().getRoleId());
        assertEquals(USER_ID, captor.getValue().getUserId());
        verify(cacheOps).del(anyCollection());
    }

    @Test
    void 回显_只返回可分配范围内的角色ID() {
        when(mapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(rel(1L), rel(2L)));
        // role 2 是模板角色，被过滤
        when(roleMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(role(1L, false, OrgNatureEnum.HEAD_COMPANY.getCode())));

        assertEquals(List.of(1L), service.findRoleIdByUserId(USER_ID));
    }

    @Test
    void 回显_无关联_返回空且不查角色() {
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());

        assertEquals(List.of(), service.findRoleIdByUserId(USER_ID));
        verifyNoInteractions(roleMapper);
    }

    @Test
    void 回显_可分配范围含组织性质条件() {
        // 构造真实公司上下文（非 mock 静态方法）；eq 取值为 null 时条件会被 mybatis-flex 整体忽略
        ContextUtil.setCurrentCompanyNature(OrgNatureEnum.HEAD_COMPANY.getCode());
        try {
            when(mapper.selectListByQuery(any(QueryWrapper.class)))
                    .thenReturn(List.of(rel(1L), rel(2L)));
            when(roleMapper.selectListByQuery(any(QueryWrapper.class)))
                    .thenReturn(List.of(role(1L, false, OrgNatureEnum.HEAD_COMPANY.getCode())));

            service.findRoleIdByUserId(USER_ID);

            ArgumentCaptor<QueryWrapper> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
            verify(roleMapper).selectListByQuery(wrapperCaptor.capture());
            String sql = wrapperCaptor.getValue().toSQL().replaceAll("\\s+", " ").toLowerCase();
            System.out.println("可分配角色查询SQL：" + sql);
            // 四个条件列必须同时存在：主键IN + 非模板 + 普通角色 + 组织性质，与 RoleController.list 对齐
            assertTrue(sql.contains("`id` in"), "条件应包含角色主键IN列，实际SQL：" + sql);
            assertTrue(sql.contains("template_role"), "条件应包含非模板角色列，实际SQL：" + sql);
            assertTrue(sql.contains("role_category"), "条件应包含角色类别列，实际SQL：" + sql);
            assertTrue(sql.contains("org_nature"), "条件应包含组织性质列，实际SQL：" + sql);
            // 组织性质取值必须来自当前公司上下文
            assertTrue(sql.contains("`org_nature` = " + OrgNatureEnum.HEAD_COMPANY.getCode()),
                    "组织性质应取当前公司性质，实际SQL：" + sql);
        } finally {
            ContextUtil.remove();
        }
    }
}
