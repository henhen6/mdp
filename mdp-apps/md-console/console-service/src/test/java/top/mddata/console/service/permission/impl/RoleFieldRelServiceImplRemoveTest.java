package top.mddata.console.service.permission.impl;

import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.common.cache.console.permission.UserFieldPermCacheKeyBuilder;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.mapper.UserRoleRelMapper;
import top.mddata.console.mapper.permission.ResourceFieldMapper;
import top.mddata.console.mapper.permission.RoleFieldRelMapper;
import top.mddata.console.service.permission.RoleService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 按角色删除字段受限关系：删表并失效相关用户的字段受限集缓存。
 *
 * @author henhen6
 * @since 2026-10-03
 */
class RoleFieldRelServiceImplRemoveTest {

    private static final Long ROLE_ID = 1L;
    private static final Long USER_ID = 9L;

    private RoleFieldRelMapper roleFieldRelMapper;
    private UserRoleRelMapper userRoleRelMapper;
    private CacheOps cacheOps;
    private RoleFieldRelServiceImpl service;

    @BeforeEach
    void setUp() {
        roleFieldRelMapper = mock(RoleFieldRelMapper.class);
        userRoleRelMapper = mock(UserRoleRelMapper.class);
        cacheOps = mock(CacheOps.class);
        service = new RoleFieldRelServiceImpl(userRoleRelMapper,
                mock(ResourceFieldMapper.class), mock(RoleService.class));
        ReflectionTestUtils.setField(service, "mapper", roleFieldRelMapper);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);
    }

    @Test
    void 删除关系并失效角色下用户的字段受限集缓存() {
        UserRoleRel userRoleRel = new UserRoleRel();
        userRoleRel.setUserId(USER_ID);
        userRoleRel.setRoleId(ROLE_ID);
        when(userRoleRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(userRoleRel));

        service.removeByRoleIds(List.of(ROLE_ID));

        verify(roleFieldRelMapper).deleteByQuery(any(QueryWrapper.class));
        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List<CacheKey>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(cacheOps).del(captor.capture());
        assertEquals(List.of(UserFieldPermCacheKeyBuilder.build(USER_ID).getKey()),
                captor.getValue().stream().map(CacheKey::getKey).toList());
    }

    @Test
    void 角色集合为空时短路() {
        service.removeByRoleIds(List.of());

        verify(roleFieldRelMapper, never()).deleteByQuery(any(QueryWrapper.class));
    }
}
