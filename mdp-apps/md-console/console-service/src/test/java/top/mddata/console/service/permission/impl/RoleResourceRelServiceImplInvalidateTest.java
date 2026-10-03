package top.mddata.console.service.permission.impl;

import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.common.cache.console.permission.UserFieldPermCacheKeyBuilder;
import top.mddata.common.cache.console.permission.UserResourceApiCacheKeyBuilder;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.mapper.UserRoleRelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 按角色失效用户级缓存（接口放行集 + 字段受限集）。
 *
 * <p>回归：userKeys 曾用 Stream.toList() 构建（Java 16+ 返回不可变列表），
 * 随后追加字段受限集 key 时抛 UnsupportedOperationException。</p>
 *
 * @author henhen6
 * @since 2026-10-03
 */
class RoleResourceRelServiceImplInvalidateTest {

    private static final Long ROLE_ID = 1L;
    private static final Long USER_ID = 9L;

    private UserRoleRelMapper userRoleRelMapper;
    private CacheOps cacheOps;
    private RoleResourceRelServiceImpl service;

    @BeforeEach
    void setUp() {
        userRoleRelMapper = mock(UserRoleRelMapper.class);
        cacheOps = mock(CacheOps.class);
        service = new RoleResourceRelServiceImpl(userRoleRelMapper);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);
    }

    @Test
    void 失效角色下用户的接口放行集与字段受限集缓存() {
        UserRoleRel rel = new UserRoleRel();
        rel.setUserId(USER_ID);
        rel.setRoleId(ROLE_ID);
        when(userRoleRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(rel));

        service.invalidateUserResourceApiCacheByRoleIds(List.of(ROLE_ID));

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List<CacheKey>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(cacheOps).del(captor.capture());
        List<String> keys = captor.getValue().stream().map(CacheKey::getKey).toList();
        assertEquals(2, keys.size());
        assertTrue(keys.contains(UserResourceApiCacheKeyBuilder.build(USER_ID).getKey()));
        assertTrue(keys.contains(UserFieldPermCacheKeyBuilder.build(USER_ID).getKey()));
    }

    @Test
    void 角色下无用户时不失效缓存() {
        when(userRoleRelMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of());

        service.invalidateUserResourceApiCacheByRoleIds(List.of(ROLE_ID));

        org.mockito.Mockito.verifyNoInteractions(cacheOps);
    }
}
