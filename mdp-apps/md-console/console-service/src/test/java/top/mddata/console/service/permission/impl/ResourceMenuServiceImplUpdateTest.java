package top.mddata.console.service.permission.impl;

import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.common.cache.console.permission.ResourceFieldUriMenuCacheKeyBuilder;
import top.mddata.console.entity.permission.ResourceMenu;
import top.mddata.console.entity.permission.RoleResourceRel;
import top.mddata.console.mapper.permission.RoleMapper;
import top.mddata.console.mapper.permission.RoleResourceRelMapper;
import top.mddata.console.mapper.permission.ResourceMenuMapper;
import top.mddata.console.service.permission.ResourceApiService;
import top.mddata.console.service.permission.ResourceFieldService;
import top.mddata.console.service.permission.RoleDataScopeRelService;
import top.mddata.console.service.permission.RoleResourceRelService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 菜单更新的下游缓存失效联动：
 * 菜单禁用/启用后，接口放行集（含字段受限集）按受影响角色失效，uri-menu 预解析映射全量失效。
 *
 * @author henhen6
 * @since 2026-10-05
 */
class ResourceMenuServiceImplUpdateTest {

    private static final Long MENU_ID = 100L;
    private static final Long CHILD_ID = 101L;
    private static final Long ROLE_ID = 1L;

    private ResourceMenuMapper resourceMenuMapper;
    private RoleResourceRelMapper roleResourceRelMapper;
    private RoleResourceRelService roleResourceRelService;
    private CacheOps cacheOps;
    private ResourceMenuServiceImpl service;

    @BeforeEach
    void setUp() {
        resourceMenuMapper = mock(ResourceMenuMapper.class);
        roleResourceRelMapper = mock(RoleResourceRelMapper.class);
        roleResourceRelService = mock(RoleResourceRelService.class);
        cacheOps = mock(CacheOps.class);
        service = new ResourceMenuServiceImpl(mock(UidGenerator.class), mock(RoleMapper.class),
                mock(RoleDataScopeRelService.class), mock(ResourceApiService.class),
                mock(ResourceFieldService.class), roleResourceRelService, roleResourceRelMapper);
        ReflectionTestUtils.setField(service, "mapper", resourceMenuMapper);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);
    }

    /** 更新后的完整菜单（模拟 getById 回查：含 treePath，数据权限开关关闭走简化路径） */
    private ResourceMenu fullMenu() {
        ResourceMenu menu = new ResourceMenu();
        menu.setId(MENU_ID);
        menu.setCode("user");
        menu.setTreePath("/1/100/");
        menu.setDataScopeState(false);
        return menu;
    }

    private ResourceMenu childMenu() {
        ResourceMenu child = new ResourceMenu();
        child.setId(CHILD_ID);
        return child;
    }

    @Test
    void 菜单更新_按子孙资源反查角色失效用户缓存并清uriMenu映射() {
        // PATCH 实体只含提交字段（禁用菜单）
        ResourceMenu patchEntity = new ResourceMenu();
        patchEntity.setId(MENU_ID);
        patchEntity.setState(false);
        when(resourceMenuMapper.selectOneById(MENU_ID)).thenReturn(fullMenu());
        when(resourceMenuMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(childMenu()));
        RoleResourceRel rel = new RoleResourceRel();
        rel.setRoleId(ROLE_ID);
        rel.setResourceId(CHILD_ID);
        when(roleResourceRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(rel));

        service.updateAfter(null, patchEntity);

        verify(roleResourceRelService).invalidateUserResourceApiCacheByRoleIds(List.of(ROLE_ID));
        verify(cacheOps).del(ResourceFieldUriMenuCacheKeyBuilder.build());
    }

    @Test
    void 无角色引用时只清uriMenu映射() {
        ResourceMenu patchEntity = new ResourceMenu();
        patchEntity.setId(MENU_ID);
        patchEntity.setState(true);
        when(resourceMenuMapper.selectOneById(MENU_ID)).thenReturn(fullMenu());
        when(resourceMenuMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of());
        when(roleResourceRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of());

        service.updateAfter(null, patchEntity);

        verify(roleResourceRelService, never()).invalidateUserResourceApiCacheByRoleIds(anyList());
        verify(cacheOps).del(ResourceFieldUriMenuCacheKeyBuilder.build());
    }
}
