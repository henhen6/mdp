package top.mddata.console.service.permission.impl;

import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.common.cache.console.permission.ResourceFieldUriMenuCacheKeyBuilder;
import top.mddata.console.entity.permission.ResourceField;
import top.mddata.console.mapper.permission.ResourceFieldMapper;
import top.mddata.console.service.permission.RoleFieldRelService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 按菜单删除字段规则配置（菜单删除时级联调用）：
 * 删字段规则、删角色字段关系、失效 uri-menu 预解析缓存与用户字段受限集缓存。
 *
 * @author henhen6
 * @since 2026-10-05
 */
class ResourceFieldServiceImplDeleteTest {

    private static final Long MENU_ID = 201L;
    private static final Long FIELD_ID = 301L;

    private ResourceFieldMapper resourceFieldMapper;
    private RoleFieldRelService roleFieldRelService;
    private CacheOps cacheOps;
    private ResourceFieldServiceImpl service;

    @BeforeEach
    void setUp() {
        resourceFieldMapper = mock(ResourceFieldMapper.class);
        roleFieldRelService = mock(RoleFieldRelService.class);
        cacheOps = mock(CacheOps.class);
        service = new ResourceFieldServiceImpl(roleFieldRelService);
        ReflectionTestUtils.setField(service, "mapper", resourceFieldMapper);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);
    }

    @Test
    void 按菜单删除字段配置_级联清理与缓存失效() {
        ResourceField field = new ResourceField();
        field.setId(FIELD_ID);
        field.setMenuId(MENU_ID);
        when(resourceFieldMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(field));

        service.deleteByMenuIds(List.of(MENU_ID));

        InOrder order = inOrder(resourceFieldMapper, roleFieldRelService, cacheOps);
        order.verify(resourceFieldMapper).deleteByQuery(any(QueryWrapper.class));
        order.verify(roleFieldRelService).remove(any(QueryWrapper.class));
        order.verify(cacheOps).del(ResourceFieldUriMenuCacheKeyBuilder.build());
        order.verify(roleFieldRelService).invalidateUserFieldPermCacheByFieldIds(List.of(FIELD_ID));
    }

    @Test
    void 菜单下无字段规则时不删不失效() {
        when(resourceFieldMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of());

        service.deleteByMenuIds(List.of(MENU_ID));

        verify(resourceFieldMapper, never()).deleteByQuery(any(QueryWrapper.class));
        verify(roleFieldRelService, never()).remove(any(QueryWrapper.class));
        verify(roleFieldRelService, never()).invalidateUserFieldPermCacheByFieldIds(any());
    }

    @Test
    void 菜单集合为空时短路() {
        service.deleteByMenuIds(List.of());

        verify(resourceFieldMapper, never()).selectListByQuery(any(QueryWrapper.class));
    }
}
