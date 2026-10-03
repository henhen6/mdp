package top.mddata.console.service.permission.impl;

import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.common.cache.console.permission.RoleDataScopeCacheKeyBuilder;
import top.mddata.console.entity.permission.RoleDataScopeRel;
import top.mddata.console.mapper.permission.RoleDataScopeRelMapper;
import top.mddata.console.mapper.permission.RoleFieldRelMapper;
import top.mddata.console.mapper.permission.RoleMapper;
import top.mddata.console.service.organization.SystemProtectService;
import top.mddata.console.service.organization.UserRoleRelService;
import top.mddata.console.service.permission.RoleAppRelService;
import top.mddata.console.service.permission.RoleResourceRelService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 删除角色的级联清除：应用权限、数据权限、字段权限、功能权限、角色绑定的用户一并删除。
 *
 * @author henhen6
 * @since 2026-10-03
 */
class RoleServiceImplRemoveTest {

    private static final Long ROLE_ID = 1L;
    private static final Long MENU_ID = 201L;

    private RoleResourceRelService roleResourceRelService;
    private RoleAppRelService roleAppRelService;
    private UserRoleRelService userRoleRelService;
    private SystemProtectService systemProtectService;
    private RoleDataScopeRelMapper roleDataScopeRelMapper;
    private RoleFieldRelMapper roleFieldRelMapper;
    private RoleMapper roleMapper;
    private CacheOps cacheOps;
    private RoleServiceImpl service;

    @BeforeEach
    void setUp() {
        roleResourceRelService = mock(RoleResourceRelService.class);
        roleAppRelService = mock(RoleAppRelService.class);
        userRoleRelService = mock(UserRoleRelService.class);
        systemProtectService = mock(SystemProtectService.class);
        roleDataScopeRelMapper = mock(RoleDataScopeRelMapper.class);
        roleFieldRelMapper = mock(RoleFieldRelMapper.class);
        roleMapper = mock(RoleMapper.class);
        cacheOps = mock(CacheOps.class);
        service = new RoleServiceImpl(roleResourceRelService, roleAppRelService, userRoleRelService,
                systemProtectService, roleDataScopeRelMapper, roleFieldRelMapper);
        ReflectionTestUtils.setField(service, "mapper", roleMapper);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);
    }

    private RoleDataScopeRel dataScopeRel() {
        RoleDataScopeRel rel = new RoleDataScopeRel();
        rel.setRoleId(ROLE_ID);
        rel.setMenuId(MENU_ID);
        return rel;
    }

    @Test
    void 删除角色_级联删除五个领域() {
        when(roleDataScopeRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(dataScopeRel()));

        service.removeByIds(List.of(ROLE_ID));

        verify(systemProtectService).checkRoleNotProtected(ROLE_ID, "删除角色");
        verify(roleDataScopeRelMapper).deleteByQuery(any(QueryWrapper.class));
        verify(roleFieldRelMapper).deleteByQuery(any(QueryWrapper.class));
        verify(roleResourceRelService).removeByRoleIds(List.of(ROLE_ID));
        verify(roleAppRelService).removeByRoleIds(List.of(ROLE_ID));
        verify(userRoleRelService).removeByRoleIds(List.of(ROLE_ID));
    }

    @Test
    void 数据权限缓存按涉及的角色与菜单失效() {
        when(roleDataScopeRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(dataScopeRel()));

        service.removeByIds(List.of(ROLE_ID));

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List<CacheKey>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(cacheOps).del(captor.capture());
        CacheKey expected = RoleDataScopeCacheKeyBuilder.build(ROLE_ID, MENU_ID);
        assertEquals(1, captor.getValue().size());
        assertEquals(expected.getKey(), captor.getValue().get(0).getKey());
    }

    @Test
    void 数据与字段权限删除先于功能权限() {
        // 顺序约束：功能权限删除内部会失效用户缓存（含 user_field_perm），
        // 放最后可兜住数据/字段权限删除的全部变更
        when(roleDataScopeRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(dataScopeRel()));

        service.removeByIds(List.of(ROLE_ID));

        InOrder order = inOrder(roleDataScopeRelMapper, roleFieldRelMapper, roleResourceRelService);
        order.verify(roleDataScopeRelMapper).deleteByQuery(any(QueryWrapper.class));
        order.verify(roleFieldRelMapper).deleteByQuery(any(QueryWrapper.class));
        order.verify(roleResourceRelService).removeByRoleIds(List.of(ROLE_ID));
    }
}
