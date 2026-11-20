package top.mddata.base.cache.repository.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import top.mddata.base.cache.redis.RedisOps;
import top.mddata.base.model.cache.CacheKey;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * initAndIncr 计数原语单测：Lua 必须以裸字符串 "0" 占位并走原生 INCR，
 * TTL 由 key.getExpire() 折算为毫秒通过 ARGV 传入（无 TTL 时传 0）。
 *
 * @author tangyh
 */
class RedisOpsImplInitAndIncrTest {

    private StringRedisTemplate stringRedisTemplate;
    private RedisOpsImpl redisOpsImpl;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        stringRedisTemplate = mock(StringRedisTemplate.class);
        RedisOps redisOps = mock(RedisOps.class);
        redisOpsImpl = new RedisOpsImpl(redisOps, stringRedisTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void initAndIncr_shouldRunLuaWithKeyAndTtlMillis() {
        CacheKey key = new CacheKey("two_factor:ticket.fail.otp:t1", Duration.ofMinutes(5));
        when(stringRedisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenReturn(3L);

        Long count = redisOpsImpl.initAndIncr(key);

        assertEquals(3L, count);

        ArgumentCaptor<RedisScript<Long>> scriptCaptor =
                ArgumentCaptor.forClass(RedisScript.class);
        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(stringRedisTemplate).execute(scriptCaptor.capture(),
                keysCaptor.capture(), argsCaptor.capture());

        assertEquals(List.of(key.getKey()), keysCaptor.getValue());
        // 5 分钟 = 300000 毫秒
        assertEquals("300000", argsCaptor.getValue()[0]);

        // 契约：占位必须是裸字符串 "0"，自增必须是原生 INCR，二者编码兼容
        String script = scriptCaptor.getValue().getScriptAsString();
        assertTrue(script.contains("redis.call('set', KEYS[1], '0')"));
        assertTrue(script.contains("redis.call('incr', KEYS[1])"));
        assertTrue(script.contains("redis.call('pexpire', KEYS[1], ttl)"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void initAndIncr_luaShouldGuardExistsAndSelfHealNonIntegerValue() {
        CacheKey key = new CacheKey("two_factor:ticket.fail.otp:t1", Duration.ofMinutes(5));
        when(stringRedisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenReturn(1L);

        redisOpsImpl.initAndIncr(key);

        ArgumentCaptor<RedisScript<Long>> scriptCaptor =
                ArgumentCaptor.forClass(RedisScript.class);
        verify(stringRedisTemplate).execute(scriptCaptor.capture(), anyList(), any());
        String script = scriptCaptor.getValue().getScriptAsString();

        // 契约：先 exists 守卫区分“缺键占位”与“已存在脏值”两条分支
        assertTrue(script.contains("redis.call('exists', KEYS[1])"));
        // 契约：键已存在但 tonumber 失败（如 NullVal 字节污染）时必须识别并进入重置分支
        assertTrue(script.contains("tonumber(redis.call('get', KEYS[1]))"));
        // 契约：自愈重置复用同一套“裸字符串 0 占位 + PEXPIRE + INCR”原语
        assertTrue(script.contains("redis.call('set', KEYS[1], '0')"));
        assertTrue(script.contains("redis.call('pexpire', KEYS[1], ttl)"));
        assertTrue(script.contains("redis.call('incr', KEYS[1])"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void initAndIncr_shouldPassZeroTtlWhenExpireMissing() {
        CacheKey key = new CacheKey("counter:no-ttl");
        when(stringRedisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenReturn(1L);

        Long count = redisOpsImpl.initAndIncr(key);

        assertEquals(1L, count);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(stringRedisTemplate).execute(any(RedisScript.class), anyList(),
                argsCaptor.capture());
        assertEquals("0", argsCaptor.getValue()[0]);
    }
}
