package top.mddata.workbench.service.twofactor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.common.constant.ConfigKey;
import top.mddata.console.facade.system.ConfigFacade;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 2FA 票据服务单元测试：一次性消费、同用户互斥作废、SecureRandom 票据、
 * initAndIncr 原子失败计数、失败次数上限配置化、blank fail fast。
 *
 * @author tangyh
 */
class TwoFactorTicketServiceTest {

    private CacheOps cacheOps;
    private ConfigFacade configFacade;
    private TwoFactorTicketService ticketService;
    private TwoFactorTicketService.TwoFactorTicketPayload payload;

    @BeforeEach
    void setUp() {
        cacheOps = mock(CacheOps.class);
        configFacade = mock(ConfigFacade.class);
        ticketService = new TwoFactorTicketService(cacheOps, configFacade);
        // 默认 5 次上限，保持既有用例语义；配置 0 或未配置表示不限制
        when(configFacade.getInteger(
                ConfigKey.Workbench.PASSWORD_ERROR_LOCK_ACCOUNT, 0)).thenReturn(5);
        payload = new TwoFactorTicketService.TwoFactorTicketPayload();
        payload.setUserId(1L);
        payload.setAuthType("CAPTCHA");
        payload.setAccount("admin");
        payload.setChannel("PHONE");
        payload.setTarget("138****5678");
        payload.setPhone("13800005678");
        payload.setEmail("admin@example.com");
    }

    @Test
    void issue_shouldGenerateSecureRandomTicket() {
        // 索引键读不到旧票
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation ->
                new CacheResult<>(((CacheKey) invocation.getArgument(0)).getKey()));

        String ticket = ticketService.issue(payload);

        // 32 字节经 URL-safe Base64 无填充编码后为 43 字符，仅含 [A-Za-z0-9_-]
        assertEquals(43, ticket.length());
        assertTrue(ticket.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void issue_shouldThrowWhenUserIdMissing() {
        payload.setUserId(null);

        assertThrows(BizException.class, () -> ticketService.issue(payload));
        verifyNoInteractions(cacheOps);
    }

    @Test
    void consume_shouldReturnPayloadWhenDeleteSucceeds() {
        // 失败计数键与索引键均无值；票据键返回载荷
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            String redisKey = key.getKey();
            Object value = redisKey.contains("ticket.fail")
                    || redisKey.contains("ticket.user") ? null : payload;
            return new CacheResult<>(redisKey, value);
        });
        String ticket = ticketService.issue(payload);

        // Redis DEL 返回实际删除条数，1 表示本次请求抢到消费权
        when(cacheOps.del(any(CacheKey.class))).thenReturn(1L);

        assertSame(payload, ticketService.consume(ticket));

        ArgumentCaptor<CacheKey> deleted = ArgumentCaptor.forClass(CacheKey.class);
        verify(cacheOps, times(2)).del(deleted.capture());
        assertTrue(deleted.getAllValues().get(0).getKey().endsWith(ticket));
    }

    @Test
    void consume_shouldReturnNullWhenTicketMissing() {
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class)))
                .thenAnswer(invocation -> new CacheResult<>(
                        ((CacheKey) invocation.getArgument(0)).getKey()));

        assertNull(ticketService.consume("not-exist"));
        verify(cacheOps, never()).del(any(CacheKey.class));
    }

    @Test
    void consume_shouldReturnNullWhenAlreadyConsumedConcurrently() {
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            Object value = key.getKey().contains("ticket.fail") ? null : payload;
            return new CacheResult<>(key.getKey(), value);
        });
        // 票据已被并发请求删除：DEL 返回 0
        when(cacheOps.del(any(CacheKey.class))).thenReturn(0L);

        assertNull(ticketService.consume("stale-ticket"));
    }

    @Test
    void consume_shouldReturnNullWhenBlockedBeforeReadingTicket() {
        // 计数键返回 5（票据在第 5 次失败时已被同步删除，这里票据键也读不到）
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            Object value = key.getKey().contains("ticket.fail") ? 5L : null;
            return new CacheResult<>(key.getKey(), value);
        });

        assertNull(ticketService.consume("t1"));
        verify(cacheOps, never()).del(any(CacheKey.class));
    }

    @Test
    void issue_shouldInvalidatePreviousTicketAndFailCounterOfSameUser() {
        // 用 key 感知的桩模拟 Redis 真实行为：索引键保存当前票据
        AtomicReference<String> currentTicket = new AtomicReference<>();
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            String redisKey = key.getKey();
            if (redisKey.contains("ticket.user")) {
                return new CacheResult<>(redisKey, currentTicket.get());
            }
            if (redisKey.contains("ticket.fail")) {
                return new CacheResult<>(redisKey);
            }
            String current = currentTicket.get();
            Object value = current != null && redisKey.endsWith(current) ? payload : null;
            return new CacheResult<>(redisKey, value);
        });

        String first = ticketService.issue(payload);
        currentTicket.set(first);
        String second = ticketService.issue(payload);
        currentTicket.set(second);

        // 第二次签发时一次性删除旧票键与旧票失败计数键
        verify(cacheOps).del(
                argThat((CacheKey k) -> k.getKey().endsWith(first)),
                argThat((CacheKey k) -> k.getKey().contains("ticket.fail")));

        // 旧票 peek 为 null，新票仍可读
        assertNull(ticketService.peek(first));
        assertNotNull(ticketService.peek(second));

        // 契约：两次签发读用户索引键、两次 peek 读计数键与票据键，全部禁止空值回写
        verify(cacheOps, times(2)).get(
                argThat((CacheKey k) -> k.getKey().contains("ticket.user")),
                noNullCache());
    }

    @Test
    void recordFail_shouldIncrAtomicallyAndInvalidateTicketAtMaxAttempts() {
        // 第 5 次失败时读票据载荷以获取 userId 清理索引键
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            Object value = key.getKey().contains("ticket.fail") ? null : payload;
            return new CacheResult<>(key.getKey(), value);
        });
        // 新原语首次调用完成裸字符串占位（携带 TTL）+ INCR，后续直接 INCR
        when(cacheOps.initAndIncr(any(CacheKey.class)))
                .thenReturn(1L, 2L, 3L, 4L, 5L);
        when(cacheOps.del(any(CacheKey.class), any(CacheKey.class))).thenReturn(2L);

        for (int i = 0; i < 4; i++) {
            ticketService.recordFail("t1");
        }
        assertThrows(BizException.class, () -> ticketService.recordFail("t1"));

        // 每次失败只调计数原语，且作用于失败计数键，不再出现 setIfAbsent/incr 组合
        verify(cacheOps, times(5)).initAndIncr(
                argThat((CacheKey k) -> k.getKey().contains("ticket.fail")));
        verify(cacheOps, never()).setIfAbsent(any(CacheKey.class), any());
        verify(cacheOps, never()).incr(any(CacheKey.class));
        // 达到上限：同步删除票据键与用户索引键，计数键保留继续拦截
        verify(cacheOps).del(
                argThat((CacheKey k) -> k.getKey().endsWith("t1")
                        && !k.getKey().contains("ticket.fail")),
                argThat((CacheKey k) -> k.getKey().contains("ticket.user")));
        // 契约：超限时读票据载荷也必须禁止空值回写
        verify(cacheOps).get(
                argThat((CacheKey k) -> k.getKey().endsWith("t1")
                        && !k.getKey().contains("ticket.fail")),
                noNullCache());
    }

    @Test
    void consume_shouldReturnNullAfterFiveFailuresBecauseTicketDeleted() {
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            Object value = key.getKey().contains("ticket.fail") ? null : payload;
            return new CacheResult<>(key.getKey(), value);
        });
        when(cacheOps.initAndIncr(any(CacheKey.class)))
                .thenReturn(1L, 2L, 3L, 4L, 5L);
        when(cacheOps.del(any(CacheKey.class), any(CacheKey.class))).thenReturn(2L);

        for (int i = 0; i < 4; i++) {
            ticketService.recordFail("t1");
        }
        assertThrows(BizException.class, () -> ticketService.recordFail("t1"));

        // 第 5 次失败时票据键与索引键已被删除
        verify(cacheOps, times(1)).del(any(CacheKey.class), any(CacheKey.class));

        // 此后消费：计数键读到 5，isBlocked 直接拦截，不再触发删除
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            Object value = key.getKey().contains("ticket.fail") ? 5L : null;
            return new CacheResult<>(key.getKey(), value);
        });
        assertNull(ticketService.consume("t1"));
        verify(cacheOps, times(1)).del(any(CacheKey.class), any(CacheKey.class));
    }

    @Test
    void recordFailAndIsBlocked_shouldNotEnforceLimitWhenMaxConfiguredZero() {
        // 上限配置为 0：不限制，只累计计数
        when(configFacade.getInteger(
                ConfigKey.Workbench.PASSWORD_ERROR_LOCK_ACCOUNT, 0)).thenReturn(0);
        // 计数累计到远超历史硬编码上限 5
        when(cacheOps.initAndIncr(any(CacheKey.class)))
                .thenReturn(1L, 2L, 3L, 6L, 99L);

        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> ticketService.recordFail("t1"));
        }

        // 计数已达 99，isBlocked 仍恒为 false，且全程未删票
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class)))
                .thenReturn(new CacheResult<>("key", 99L));
        assertFalse(ticketService.isBlocked("t1"));
        verify(cacheOps, never()).del(any(CacheKey.class), any(CacheKey.class));
    }

    @Test
    void recordFail_shouldInvalidateTicketWhenCountReachesConfiguredLimitOfThree() {
        // 上限配置为 3：第 3 次失败即删票作废
        when(configFacade.getInteger(
                ConfigKey.Workbench.PASSWORD_ERROR_LOCK_ACCOUNT, 0)).thenReturn(3);
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            Object value = key.getKey().contains("ticket.fail") ? null : payload;
            return new CacheResult<>(key.getKey(), value);
        });
        when(cacheOps.initAndIncr(any(CacheKey.class))).thenReturn(1L, 2L, 3L);
        when(cacheOps.del(any(CacheKey.class), any(CacheKey.class))).thenReturn(2L);

        ticketService.recordFail("t1");
        ticketService.recordFail("t1");
        assertThrows(BizException.class, () -> ticketService.recordFail("t1"));

        verify(cacheOps, times(3)).initAndIncr(
                argThat((CacheKey k) -> k.getKey().contains("ticket.fail")));
        // 第 3 次失败即同步删除票据键与用户索引键
        verify(cacheOps).del(
                argThat((CacheKey k) -> k.getKey().endsWith("t1")
                        && !k.getKey().contains("ticket.fail")),
                argThat((CacheKey k) -> k.getKey().contains("ticket.user")));

        // 此后计数为 3，isBlocked 按新上限持续拦截
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            Object value = key.getKey().contains("ticket.fail") ? 3L : null;
            return new CacheResult<>(key.getKey(), value);
        });
        assertTrue(ticketService.isBlocked("t1"));
    }

    @Test
    void peek_shouldNotConsumeTicket() {
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            // 失败计数键无计数，票据键返回载荷
            Object value = key.getKey().contains("ticket.fail") ? null : payload;
            return new CacheResult<>(key.getKey(), value);
        });

        assertSame(payload, ticketService.peek("t1"));
        verify(cacheOps, never()).del(any(CacheKey.class));
    }

    @Test
    void peek_shouldReturnNullWhenBlockedAfterMaxFailures() {
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenReturn(new CacheResult<>("key", 5L));

        assertNull(ticketService.peek("t1"));
        verify(cacheOps, never()).del(any(CacheKey.class));
    }

    @Test
    void isBlockedPeekConsume_shouldReadCounterAndTicketWithoutNullValueWriteBack() {
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            // 失败计数键 miss（返回 null），票据键返回载荷
            Object value = key.getKey().contains("ticket.fail") ? null : payload;
            return new CacheResult<>(key.getKey(), value);
        });
        when(cacheOps.del(any(CacheKey.class))).thenReturn(1L);

        ticketService.isBlocked("tk");
        ticketService.peek("tk");
        ticketService.consume("tk");

        // 读次数：isBlocked 1 + peek 2（含内部 isBlocked）+ consume 2（含内部 isBlocked）= 5，
        // 全部必须显式带 false，杜绝 GET miss 回写 NullVal 污染计数键/票据键
        verify(cacheOps, times(5)).get(any(CacheKey.class), noNullCache());
        // 计数键读取单独再验一次，正是它被污染后会让 recordFail 的 INCR 报错
        verify(cacheOps, atLeastOnce()).get(
                argThat((CacheKey k) -> k.getKey().contains("ticket.fail")),
                noNullCache());
        // 不允许走缺省可变参数（框架默认 cacheNullVal=true）的读法
        verify(cacheOps, never()).get(any(CacheKey.class));
    }

    @Test
    void entryMethods_shouldFailFastWhenTicketBlank() {
        assertThrows(BizException.class, () -> ticketService.peek(" "));
        assertThrows(BizException.class, () -> ticketService.consume(null));
        assertThrows(BizException.class, () -> ticketService.recordFail(""));
        assertThrows(BizException.class, () -> ticketService.isBlocked(" "));
        assertThrows(BizException.class,
                () -> ticketService.updatePayload("", payload));
        verifyNoInteractions(cacheOps);
    }

    /**
     * 契约匹配器：get 的 cacheNullValues 可变参数必须且只含 false，
     * 禁止 GET miss 时框架向计数键/票据键回写 NullVal 占位。
     * 重写 type() 声明按整个可变参数数组匹配（Mockito 5 对 varargs 整体匹配的约定）。
     */
    private static final class NoNullCacheMatcher implements ArgumentMatcher<boolean[]> {
        @Override
        public boolean matches(boolean[] arr) {
            return arr != null && arr.length == 1 && !arr[0];
        }

        @Override
        public Class<?> type() {
            return boolean[].class;
        }
    }

    private static boolean[] noNullCache() {
        return argThat(new NoNullCacheMatcher());
    }
}
