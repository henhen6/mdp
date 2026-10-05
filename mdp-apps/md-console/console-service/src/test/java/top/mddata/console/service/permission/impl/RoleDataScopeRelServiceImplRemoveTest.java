package top.mddata.console.service.permission.impl;

import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.common.cache.console.permission.RoleDataScopeCacheKeyBuilder;
import top.mddata.console.entity.permission.RoleDataScopeRel;
import top.mddata.console.mapper.permission.ResourceMenuMapper;
import top.mddata.console.mapper.permission.RoleDataScopeRelMapper;
import top.mddata.console.service.permission.RoleService;
import top.mddata.open.facade.admin.AppFacade;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 按角色删除数据权限授权：删表并按涉及的角色与菜单失效 RoleDataScope 缓存。
 *
 * @author henhen6
 * @since 2026-10-03
 */
class RoleDataScopeRelServiceImplRemoveTest {

    private static final Long ROLE_ID = 1L;
    private static final Long MENU_ID = 201L;

    private RoleDataScopeRelMapper roleDataScopeRelMapper;
    private CacheOps cacheOps;
    private RoleDataScopeRelServiceImpl service;

    @BeforeEach
    void setUp() {
        roleDataScopeRelMapper = mock(RoleDataScopeRelMapper.class);
        cacheOps = mock(CacheOps.class);
        service = new RoleDataScopeRelServiceImpl(mock(RoleService.class), mock(ResourceMenuMapper.class),
                mock(AppFacade.class));
        ReflectionTestUtils.setField(service, "mapper", roleDataScopeRelMapper);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);
    }

    private RoleDataScopeRel rel() {
        RoleDataScopeRel rel = new RoleDataScopeRel();
        rel.setRoleId(ROLE_ID);
        rel.setMenuId(MENU_ID);
        return rel;
    }

    @Test
    void 删除授权并按涉及的角色与菜单失效缓存() {
        when(roleDataScopeRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(rel()));

        service.removeByRoleIds(List.of(ROLE_ID));

        verify(roleDataScopeRelMapper).deleteByQuery(any(QueryWrapper.class));
        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List<CacheKey>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(cacheOps).del(captor.capture());
        assertEquals(List.of(RoleDataScopeCacheKeyBuilder.build(ROLE_ID, MENU_ID).getKey()),
                captor.getValue().stream().map(CacheKey::getKey).toList());
    }

    @Test
    void 无授权关系时不删表不失效缓存() {
        when(roleDataScopeRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of());

        service.removeByRoleIds(List.of(ROLE_ID));

        verify(roleDataScopeRelMapper, never()).deleteByQuery(any(QueryWrapper.class));
        verify(cacheOps, never()).del(anyList());
    }

    @Test
    void 角色集合为空时短路() {
        service.removeByRoleIds(List.of());

        verify(roleDataScopeRelMapper, never()).selectListByQuery(any(QueryWrapper.class));
        verify(roleDataScopeRelMapper, never()).deleteByQuery(any(QueryWrapper.class));
    }

    @Test
    void 按菜单删除授权并失效缓存() {
        when(roleDataScopeRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(rel()));

        service.removeByMenuIds(List.of(MENU_ID));

        verify(roleDataScopeRelMapper).deleteByQuery(any(QueryWrapper.class));
        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List<CacheKey>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(cacheOps).del(captor.capture());
        assertEquals(List.of(RoleDataScopeCacheKeyBuilder.build(ROLE_ID, MENU_ID).getKey()),
                captor.getValue().stream().map(CacheKey::getKey).toList());
    }

    @Test
    void 按菜单删除_菜单集合为空时短路() {
        service.removeByMenuIds(List.of());

        verify(roleDataScopeRelMapper, never()).selectListByQuery(any(QueryWrapper.class));
        verify(roleDataScopeRelMapper, never()).deleteByQuery(any(QueryWrapper.class));
    }
}
