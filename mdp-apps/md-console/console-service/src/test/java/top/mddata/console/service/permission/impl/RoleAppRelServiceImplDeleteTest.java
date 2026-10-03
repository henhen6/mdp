package top.mddata.console.service.permission.impl;

import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.common.cache.console.permission.RoleDataScopeCacheKeyBuilder;
import top.mddata.console.dto.permission.RoleAppRelDto;
import top.mddata.console.entity.permission.ResourceField;
import top.mddata.console.entity.permission.ResourceMenu;
import top.mddata.console.entity.permission.RoleDataScopeRel;
import top.mddata.console.mapper.permission.ResourceFieldMapper;
import top.mddata.console.mapper.permission.ResourceMenuMapper;
import top.mddata.console.mapper.permission.RoleAppRelMapper;
import top.mddata.console.mapper.permission.RoleDataScopeRelMapper;
import top.mddata.console.mapper.permission.RoleFieldRelMapper;
import top.mddata.console.service.permission.RoleResourceRelService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 取消应用授权的级联清除：同一角色+应用下的功能权限、数据权限、字段权限全部删除。
 *
 * @author henhen6
 * @since 2026-10-03
 */
class RoleAppRelServiceImplDeleteTest {

    private static final Long ROLE_ID = 1L;
    private static final Long APP_ID = 100L;
    private static final Long MENU_ID = 201L;
    private static final Long FIELD_ID = 301L;

    private RoleAppRelMapper roleAppRelMapper;
    private RoleResourceRelService roleResourceRelService;
    private RoleDataScopeRelMapper roleDataScopeRelMapper;
    private RoleFieldRelMapper roleFieldRelMapper;
    private ResourceMenuMapper resourceMenuMapper;
    private ResourceFieldMapper resourceFieldMapper;
    private CacheOps cacheOps;
    private RoleAppRelServiceImpl service;

    @BeforeEach
    void setUp() {
        roleAppRelMapper = mock(RoleAppRelMapper.class);
        roleResourceRelService = mock(RoleResourceRelService.class);
        roleDataScopeRelMapper = mock(RoleDataScopeRelMapper.class);
        roleFieldRelMapper = mock(RoleFieldRelMapper.class);
        resourceMenuMapper = mock(ResourceMenuMapper.class);
        resourceFieldMapper = mock(ResourceFieldMapper.class);
        cacheOps = mock(CacheOps.class);
        service = new RoleAppRelServiceImpl(roleResourceRelService, roleDataScopeRelMapper,
                roleFieldRelMapper, resourceMenuMapper, resourceFieldMapper);
        ReflectionTestUtils.setField(service, "mapper", roleAppRelMapper);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);
    }

    private RoleAppRelDto dto() {
        RoleAppRelDto dto = new RoleAppRelDto();
        dto.setRoleId(ROLE_ID);
        dto.setAppIdList(List.of(APP_ID));
        return dto;
    }

    private ResourceMenu menu(Long id) {
        ResourceMenu menu = new ResourceMenu();
        menu.setId(id);
        menu.setAppId(APP_ID);
        return menu;
    }

    @Test
    void 取消应用权限_级联删除三类权限且功能权限最后() {
        when(resourceMenuMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(menu(MENU_ID)));
        RoleDataScopeRel rel = new RoleDataScopeRel();
        rel.setRoleId(ROLE_ID);
        rel.setMenuId(MENU_ID);
        when(roleDataScopeRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(rel));
        ResourceField field = new ResourceField();
        field.setId(FIELD_ID);
        field.setMenuId(MENU_ID);
        when(resourceFieldMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(field));

        service.delete(dto());

        // 顺序：数据权限 → 字段权限 → 功能权限（其用户缓存失效兜底）→ 应用关联
        InOrder order = inOrder(roleDataScopeRelMapper, roleFieldRelMapper,
                roleResourceRelService, roleAppRelMapper);
        order.verify(roleDataScopeRelMapper).deleteByQuery(any(QueryWrapper.class));
        order.verify(roleFieldRelMapper).deleteByQuery(any(QueryWrapper.class));
        order.verify(roleResourceRelService).removeByRoleIdAndAppIds(ROLE_ID, List.of(APP_ID));
        order.verify(roleAppRelMapper).deleteByQuery(any(QueryWrapper.class));
    }

    @Test
    void 数据权限缓存按涉及的菜单失效() {
        when(resourceMenuMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(menu(MENU_ID)));
        RoleDataScopeRel rel = new RoleDataScopeRel();
        rel.setRoleId(ROLE_ID);
        rel.setMenuId(MENU_ID);
        when(roleDataScopeRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(rel));
        when(resourceFieldMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of());

        service.delete(dto());

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List<CacheKey>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(cacheOps).del(captor.capture());
        CacheKey expected = RoleDataScopeCacheKeyBuilder.build(ROLE_ID, MENU_ID);
        assertEquals(1, captor.getValue().size());
        assertEquals(expected.getKey(), captor.getValue().get(0).getKey());
    }

    @Test
    void 应用下无菜单时跳过数据与字段权限删除() {
        when(resourceMenuMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of());

        service.delete(dto());

        verify(roleDataScopeRelMapper, never()).deleteByQuery(any(QueryWrapper.class));
        verify(roleFieldRelMapper, never()).deleteByQuery(any(QueryWrapper.class));
        verify(cacheOps, never()).del(anyList());
        // 功能权限与应用关联仍照常删除
        verify(roleResourceRelService).removeByRoleIdAndAppIds(ROLE_ID, List.of(APP_ID));
        verify(roleAppRelMapper).deleteByQuery(any(QueryWrapper.class));
    }

    @Test
    void 菜单下无数据权限关系时不删不失效缓存() {
        when(resourceMenuMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(menu(MENU_ID)));
        when(roleDataScopeRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of());
        when(resourceFieldMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of());

        service.delete(dto());

        verify(roleDataScopeRelMapper, never()).deleteByQuery(any(QueryWrapper.class));
        verify(cacheOps, never()).del(anyList());
    }
}
