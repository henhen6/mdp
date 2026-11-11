package top.mddata.workbench.service.impl;

import cn.hutool.core.util.ReflectUtil;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import top.mddata.base.exception.BizException;
import top.mddata.common.entity.UserSocialAuth;
import top.mddata.common.mapper.UserSocialAuthMapper;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.social.model.SocialIdentity;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UserSocialAuthServiceImpl} 第三方身份绑定持久化行为单测。
 *
 * <p>覆盖重复绑定拒绝、绑定成功落库、唯一键冲突转换、按身份/用户查询、
 * 计数、解绑与转移的条件更新（必须同时匹配绑定记录和当前用户）。</p>
 *
 * @author tangyh
 */
class UserSocialAuthServiceImplTest {

    private UserSocialAuthMapper mapper;
    private UserSocialAuthServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(UserSocialAuthMapper.class);
        service = new UserSocialAuthServiceImpl();
        // mapper 字段定义在 MyBatis-Flex ServiceImpl 父类中，通过反射注入模拟对象
        ReflectUtil.setFieldValue(service, "mapper", mapper);
    }

    @Test
    void 第三方身份已绑定其他用户时拒绝重复绑定() {
        SocialIdentity identity = identity();
        UserSocialAuth existing = auth(9L, 2L);
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(existing);

        assertThrows(BizException.class, () -> service.bind(1L, identity));
        verify(mapper, never()).insert(any(UserSocialAuth.class), anyBoolean());
    }

    @Test
    void 当前用户已绑定该平台时拒绝重复绑定() {
        UserSocialAuth existing = auth(9L, 1L);
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null, existing);

        assertThrows(BizException.class, () -> service.bind(1L, identity()));
        verify(mapper, never()).insert(any(UserSocialAuth.class), anyBoolean());
    }

    @Test
    void 绑定成功时持久化全部身份字段() {
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        when(mapper.insert(any(UserSocialAuth.class), anyBoolean())).thenReturn(1);

        UserSocialAuth bound = service.bind(1L, identity());

        ArgumentCaptor<UserSocialAuth> captor = ArgumentCaptor.forClass(UserSocialAuth.class);
        verify(mapper).insert(captor.capture(), anyBoolean());
        UserSocialAuth entity = captor.getValue();
        assertSame(bound, entity);
        assertEquals(1L, entity.getUserId());
        assertEquals("QQ", entity.getProvider());
        assertEquals("client-id", entity.getClientId());
        assertEquals("open-id", entity.getOpenId());
        assertEquals("union-id", entity.getUnionId());
        assertEquals("测试用户", entity.getNickname());
        assertNotNull(entity.getBoundAt());
    }

    @Test
    void 绑定遇到唯一键冲突时转为业务异常() {
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        when(mapper.insert(any(UserSocialAuth.class), anyBoolean()))
                .thenThrow(new DuplicateKeyException("重复键"));

        assertThrows(BizException.class, () -> service.bind(1L, identity()));
    }

    @Test
    void 按身份查询时使用平台客户端和openid条件() {
        UserSocialAuth existing = auth(9L, null);
        ArgumentCaptor<QueryWrapper> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        when(mapper.selectOneByQuery(captor.capture())).thenReturn(existing);

        Optional<UserSocialAuth> result = service.findByIdentity(identity());

        assertEquals(9L, result.orElseThrow().getId());
        Map<String, Object> conditions = whereConditions(captor.getValue());
        assertEquals("QQ", conditions.get("provider"));
        assertEquals("client-id", conditions.get("client_id"));
        assertEquals("open-id", conditions.get("open_id"));
    }

    @Test
    void 按用户和平台查询时同时带上两个条件() {
        UserSocialAuth existing = auth(9L, null);
        ArgumentCaptor<QueryWrapper> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        when(mapper.selectOneByQuery(captor.capture())).thenReturn(existing);

        Optional<UserSocialAuth> result = service.findByUserAndProvider(1L, SocialProviderEnum.QQ);

        assertEquals(9L, result.orElseThrow().getId());
        Map<String, Object> conditions = whereConditions(captor.getValue());
        assertEquals(1L, conditions.get("user_id"));
        assertEquals("QQ", conditions.get("provider"));
    }

    @Test
    void 列出用户的全部绑定记录() {
        UserSocialAuth first = auth(1L, null);
        UserSocialAuth second = auth(2L, null);
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(first, second));

        List<UserSocialAuth> result = service.listByUser(1L);

        assertEquals(2, result.size());
    }

    @Test
    void 统计用户绑定数量时使用计数查询() {
        when(mapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(3L);

        long count = service.countByUser(1L);

        assertEquals(3L, count);
    }

    @Test
    void 解绑必须同时匹配绑定记录和当前用户() {
        when(mapper.logicalDeleteByUser(any(), any(), any(), any(), any())).thenReturn(0);

        assertThrows(BizException.class, () -> service.unbind(9L, 1L));
    }

    @Test
    void 解绑成功时按绑定记录和当前用户条件逻辑删除() {
        when(mapper.logicalDeleteByUser(any(), any(), any(), any(), any())).thenReturn(1);

        service.unbind(9L, 1L);

        verify(mapper).logicalDeleteByUser(eq(9L), eq(1L), eq(1L), anyLong(), any());
    }

    @Test
    void 转移时源用户不匹配则快速失败() {
        when(mapper.transferUser(any(), any(), any(), any(), any())).thenReturn(0);

        assertThrows(BizException.class, () -> service.transfer(9L, 1L, 2L, 1L));
    }

    @Test
    void 转移成功时更新绑定归属用户() {
        when(mapper.transferUser(any(), any(), any(), any(), any())).thenReturn(1);

        service.transfer(9L, 1L, 2L, 1L);

        verify(mapper).transferUser(eq(9L), eq(1L), eq(2L), eq(1L), any());
    }

    @Test
    void 转移遇到唯一键冲突时转为业务异常() {
        when(mapper.transferUser(any(), any(), any(), any(), any()))
                .thenThrow(new DuplicateKeyException("目标账号已绑定该平台"));

        assertThrows(BizException.class, () -> service.transfer(9L, 1L, 2L, 1L));
    }

    @Test
    void 平台枚举序列化使用前端编码并可反向解析() {
        assertEquals("wechat-open", SocialProviderEnum.WECHAT_OPEN.getCode());
        assertEquals("qq", SocialProviderEnum.QQ.getCode());
        assertEquals("dingtalk", SocialProviderEnum.DINGTALK.getCode());
        assertSame(SocialProviderEnum.QQ, SocialProviderEnum.fromCode("QQ"));
        assertThrows(BizException.class, () -> SocialProviderEnum.fromCode("unknown"));
    }

    @Test
    void 身份模型支持构建器和链式赋值() {
        SocialIdentity identity = SocialIdentity.builder()
                .provider(SocialProviderEnum.QQ)
                .clientId("client-id")
                .openId("open-id")
                .unionId("union-id")
                .nickname("测试用户")
                .build();

        SocialIdentity chained = new SocialIdentity()
                .setProvider(SocialProviderEnum.DINGTALK)
                .setClientId("another");

        assertTrue(identity.toString().contains("测试用户"));
        assertEquals(SocialProviderEnum.DINGTALK, chained.getProvider());
        assertEquals("another", chained.getClientId());
    }

    /**
     * 提取 where 条件链上的全部有效条件（列名 -> 参数值）。
     * 不比对整段 SQL，避免空格、方言差异造成脆弱断言。
     */
    private Map<String, Object> whereConditions(QueryWrapper wrapper) {
        QueryCondition start = ReflectUtil.invoke(wrapper, "getWhereQueryCondition");
        Set<QueryCondition> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Map<String, Object> conditions = new LinkedHashMap<>();
        collectConditions(start, visited, conditions);
        return conditions;
    }

    private void collectConditions(QueryCondition condition, Set<QueryCondition> visited,
                                   Map<String, Object> conditions) {
        if (condition == null || !visited.add(condition)) {
            return;
        }
        if (condition.getColumn() != null) {
            conditions.put(condition.getColumn().getName(), condition.getValue());
        }
        collectConditions(ReflectUtil.invoke(condition, "getPrevEffectiveCondition"),
                visited, conditions);
        collectConditions(ReflectUtil.invoke(condition, "getNextEffectiveCondition"),
                visited, conditions);
    }

    private UserSocialAuth auth(Long id, Long userId) {
        UserSocialAuth auth = new UserSocialAuth();
        auth.setId(id);
        auth.setUserId(userId);
        return auth;
    }

    private SocialIdentity identity() {
        return SocialIdentity.builder()
                .provider(SocialProviderEnum.QQ)
                .clientId("client-id")
                .openId("open-id")
                .unionId("union-id")
                .nickname("测试用户")
                .build();
    }
}
