package top.mddata.common.cache.workbench;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.mddata.base.model.cache.CacheKey;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 双因素认证缓存 Key 测试：校验 key 形态与烘入的过期时间
 *
 * @author tangyh
 * @since 2026/11/16
 */
class TwoFactorCacheKeyBuilderTest {

    @Test
    @DisplayName("ticket Key 以 two_factor:ticket:票据 结尾且5分钟过期")
    void ticketKey形态与过期时间正确() {
        CacheKey cacheKey = TwoFactorCacheKeyBuilder.ticket("abc");
        assertNotNull(cacheKey);
        assertTrue(cacheKey.getKey().endsWith("two_factor:ticket:abc"));
        assertEquals(Duration.ofMinutes(5), cacheKey.getExpire());
    }

    @Test
    @DisplayName("ticketIndex Key 含 two_factor:ticket.user:userId 且5分钟过期")
    void ticketIndexKey形态与过期时间正确() {
        CacheKey cacheKey = TwoFactorCacheKeyBuilder.ticketIndex(1L);
        assertNotNull(cacheKey);
        assertTrue(cacheKey.getKey().contains("two_factor:ticket.user:1"));
        assertEquals(Duration.ofMinutes(5), cacheKey.getExpire());
    }

    @Test
    @DisplayName("otp Key 含场景与目标且5分钟过期")
    void otpKey形态与过期时间正确() {
        CacheKey cacheKey = TwoFactorCacheKeyBuilder.otp("login", "13800001111");
        assertNotNull(cacheKey);
        assertTrue(cacheKey.getKey().contains("two_factor:login.otp:13800001111"));
        assertEquals(Duration.ofMinutes(5), cacheKey.getExpire());
    }

    @Test
    @DisplayName("otpLimit Key 含场景与目标且60秒过期")
    void otpLimitKey形态与过期时间正确() {
        CacheKey cacheKey = TwoFactorCacheKeyBuilder.otpLimit("login", "13800001111");
        assertNotNull(cacheKey);
        assertTrue(cacheKey.getKey().contains("two_factor:login.otp.limit:13800001111"));
        // Duration 会规范化，60 秒与 1 分钟等值
        assertEquals(Duration.ofSeconds(60), cacheKey.getExpire());
    }

    @Test
    @DisplayName("未配置全局前缀时 key 不含前缀段")
    void 前缀为null时key不含前缀段() {
        CacheKey cacheKey = TwoFactorCacheKeyBuilder.ticket("abc");
        assertTrue(cacheKey.getKey().startsWith("two_factor:"));
    }
}
