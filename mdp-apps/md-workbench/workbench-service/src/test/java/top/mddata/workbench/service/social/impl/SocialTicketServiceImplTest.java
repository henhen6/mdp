package top.mddata.workbench.service.social.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.workbench.enumeration.SocialActionEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.enumeration.SocialReauthMethodEnum;
import top.mddata.workbench.service.social.SocialTicketService;
import top.mddata.workbench.service.social.model.PendingSocialUser;
import top.mddata.workbench.service.social.model.SocialIdentity;
import top.mddata.workbench.service.social.model.SocialReauth;
import top.mddata.workbench.service.social.model.SocialState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SocialTicketServiceImpl} 票据有效期、用途隔离与一次性消费行为测试。
 *
 * <p>验证 state、pendingTicket、reauthTicket 三类票据：高熵随机生成、平台/动作/用户
 * 用途校验、{@code setIfAbsent} 消费标记防并发重放，以及 pending 失败 5 次失效。</p>
 *
 * @author tangyh
 */
class SocialTicketServiceImplTest {

    private static final String TICKET = "ticket-1";

    private CacheOps cacheOps;
    private SocialTicketService ticketService;

    @BeforeEach
    void setUp() {
        cacheOps = mock(CacheOps.class);
        ticketService = new SocialTicketServiceImpl(cacheOps);
    }

    // ------------------------------------------------------------------ state

    @Test
    void createState写入缓存并返回随机票据() {
        SocialState state = loginState();

        String ticket = ticketService.createState(state);

        assertNotNull(ticket);
        verify(cacheOps).set(any(CacheKey.class), eq(state));
    }

    @Test
    void createState两次生成的票据不重复且为urlSafe随机串() {
        String first = ticketService.createState(loginState());
        String second = ticketService.createState(loginState());

        assertNotEquals(first, second);
        assertEquals(43, first.length());
        assertTrue(first.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void state只能消费一次() {
        SocialState state = loginState();
        when(cacheOps.get(any(CacheKey.class)))
                .thenReturn(new CacheResult<>("key", state));
        when(cacheOps.setIfAbsent(any(CacheKey.class), any()))
                .thenReturn(true, false);

        assertSame(state, ticketService.consumeState("state-1",
                SocialProviderEnum.QQ, SocialActionEnum.LOGIN));
        assertThrows(BizException.class, () -> ticketService.consumeState("state-1",
                SocialProviderEnum.QQ, SocialActionEnum.LOGIN));
    }

    @Test
    void state不存在或已过期时拒绝且不写消费标记() {
        when(cacheOps.get(any(CacheKey.class))).thenReturn(new CacheResult<>("key"));

        assertThrows(BizException.class, () -> ticketService.consumeState(TICKET,
                SocialProviderEnum.QQ, SocialActionEnum.LOGIN));
        verify(cacheOps, never()).setIfAbsent(any(CacheKey.class), any());
    }

    @Test
    void state平台不匹配时拒绝() {
        mockState(loginState());

        assertThrows(BizException.class, () -> ticketService.consumeState(TICKET,
                SocialProviderEnum.DINGTALK, SocialActionEnum.LOGIN));
        verify(cacheOps, never()).setIfAbsent(any(CacheKey.class), any());
    }

    @Test
    void state动作不匹配时拒绝() {
        mockState(loginState());

        assertThrows(BizException.class, () -> ticketService.consumeState(TICKET,
                SocialProviderEnum.QQ, SocialActionEnum.BIND));
        verify(cacheOps, never()).setIfAbsent(any(CacheKey.class), any());
    }

    // ---------------------------------------------------------------- pending

    @Test
    void createPending保存已验证第三方身份并返回票据() {
        SocialIdentity identity = identity();

        String ticket = ticketService.createPending(identity);

        assertNotNull(ticket);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(cacheOps).set(any(CacheKey.class), captor.capture());
        PendingSocialUser saved = (PendingSocialUser) captor.getValue();
        assertSame(identity, saved.getIdentity());
    }

    @Test
    void getPending返回待选择用户() {
        PendingSocialUser pending = new PendingSocialUser(identity());
        when(cacheOps.get(any(CacheKey.class)))
                .thenReturn(new CacheResult<>("key", pending));
        when(cacheOps.exists(any(CacheKey.class))).thenReturn(false);

        assertSame(pending, ticketService.getPending(TICKET));
    }

    @Test
    void getPending不存在时拒绝() {
        when(cacheOps.get(any(CacheKey.class))).thenReturn(new CacheResult<>("key"));

        assertThrows(BizException.class, () -> ticketService.getPending(TICKET));
    }

    @Test
    void getPending已消费时拒绝() {
        when(cacheOps.get(any(CacheKey.class)))
                .thenReturn(new CacheResult<>("key", new PendingSocialUser(identity())));
        when(cacheOps.exists(any(CacheKey.class))).thenReturn(true);

        assertThrows(BizException.class, () -> ticketService.getPending(TICKET));
    }

    @Test
    void consumePending通过setIfAbsent原子消费() {
        mockPending();
        when(cacheOps.setIfAbsent(any(CacheKey.class), any())).thenReturn(true);

        ticketService.consumePending(TICKET);

        verify(cacheOps).setIfAbsent(any(CacheKey.class), eq(true));
    }

    @Test
    void consumePending并发重放时第二个请求被拒绝() {
        mockPending();
        when(cacheOps.setIfAbsent(any(CacheKey.class), any()))
                .thenReturn(true, false);

        ticketService.consumePending(TICKET);
        assertThrows(BizException.class, () -> ticketService.consumePending(TICKET));
    }

    @Test
    void 密码错误前四次不消费票据也不删除() {
        when(cacheOps.incr(any(CacheKey.class))).thenReturn(1L, 2L, 3L, 4L);

        for (int i = 0; i < 4; i++) {
            ticketService.registerPendingFailure(TICKET);
        }

        verify(cacheOps, never()).del(any(CacheKey.class));
        verify(cacheOps, never()).setIfAbsent(any(CacheKey.class), eq(true));
    }

    @Test
    void 密码错误第五次使pending票据失效并清理计数() {
        when(cacheOps.incr(any(CacheKey.class))).thenReturn(1L, 2L, 3L, 4L, 5L);

        for (int i = 0; i < 5; i++) {
            ticketService.registerPendingFailure(TICKET);
        }

        ArgumentCaptor<CacheKey> keys = ArgumentCaptor.forClass(CacheKey.class);
        verify(cacheOps).del(keys.capture(), keys.capture());
        boolean hasPending = keys.getAllValues().stream()
                .anyMatch(key -> key.getKey().endsWith(TICKET));
        boolean hasFailed = keys.getAllValues().stream()
                .anyMatch(key -> key.getKey().contains("failed"));
        assertTrue(hasPending && hasFailed);
    }

    // ---------------------------------------------------------------- reauth

    @Test
    void createReauth保存票据上下文并返回票据() {
        SocialReauth reauth = reauth(SocialActionEnum.BIND);

        String ticket = ticketService.createReauth(reauth);

        assertNotNull(ticket);
        verify(cacheOps).set(any(CacheKey.class), eq(reauth));
    }

    @Test
    void 二次验证票据不能跨动作使用() {
        mockReauth(reauth(SocialActionEnum.BIND));

        assertThrows(BizException.class, () -> ticketService.getReauth(TICKET, 1L,
                SocialActionEnum.UNBIND, SocialProviderEnum.QQ));
    }

    @Test
    void 二次验证票据不能跨用户使用() {
        mockReauth(reauth(SocialActionEnum.BIND));

        assertThrows(BizException.class, () -> ticketService.getReauth(TICKET, 2L,
                SocialActionEnum.BIND, SocialProviderEnum.QQ));
    }

    @Test
    void 二次验证票据不能跨平台使用() {
        mockReauth(reauth(SocialActionEnum.BIND));

        assertThrows(BizException.class, () -> ticketService.getReauth(TICKET, 1L,
                SocialActionEnum.BIND, SocialProviderEnum.DINGTALK));
    }

    @Test
    void getReauth已消费时拒绝() {
        mockReauth(reauth(SocialActionEnum.BIND));
        when(cacheOps.exists(any(CacheKey.class))).thenReturn(true);

        assertThrows(BizException.class, () -> ticketService.getReauth(TICKET, 1L,
                SocialActionEnum.BIND, SocialProviderEnum.QQ));
    }

    @Test
    void consumeReauth原子消费且不可重复消费() {
        mockReauth(reauth(SocialActionEnum.UNBIND));
        when(cacheOps.setIfAbsent(any(CacheKey.class), any()))
                .thenReturn(true, false);

        ticketService.consumeReauth(TICKET);
        assertThrows(BizException.class, () -> ticketService.consumeReauth(TICKET));
    }

    // --------------------------------------------------------------- helpers

    private void mockState(SocialState state) {
        when(cacheOps.get(any(CacheKey.class)))
                .thenReturn(new CacheResult<>("key", state));
    }

    private void mockPending() {
        when(cacheOps.get(any(CacheKey.class)))
                .thenReturn(new CacheResult<>("key", new PendingSocialUser(identity())));
    }

    private void mockReauth(SocialReauth reauth) {
        when(cacheOps.get(any(CacheKey.class)))
                .thenReturn(new CacheResult<>("key", reauth));
        when(cacheOps.exists(any(CacheKey.class))).thenReturn(false);
    }

    private SocialState loginState() {
        return SocialState.builder()
                .provider(SocialProviderEnum.QQ)
                .action(SocialActionEnum.LOGIN)
                .build();
    }

    private SocialReauth reauth(SocialActionEnum action) {
        return SocialReauth.builder()
                .userId(1L)
                .action(action)
                .provider(SocialProviderEnum.QQ)
                .method(SocialReauthMethodEnum.PASSWORD)
                .build();
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
