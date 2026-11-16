package top.mddata.common.cache.workbench;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 指纹登录缓存 Key 测试
 *
 * @author tangyh
 * @since 2026/11/13
 */
class FingerprintCacheKeyBuilderTest {

    @Test
    @DisplayName("挑战Key包含挑战ID且5分钟过期")
    void 挑战Key包含挑战ID且5分钟过期() {
        var cacheKey = new FingerprintChallengeCacheKeyBuilder().key("abc123");
        assertNotNull(cacheKey);
        assertEquals(Duration.ofMinutes(5), cacheKey.getExpire());
        assertTrue(cacheKey.getKey().contains("fingerprint.challenge"));
        assertTrue(cacheKey.getKey().contains("abc123"));
    }

    @Test
    @DisplayName("密码票据Key包含userId且5分钟过期")
    void 密码票据Key包含userId且5分钟过期() {
        var cacheKey = new FingerprintPwdVerifiedCacheKeyBuilder().key("12345");
        assertNotNull(cacheKey);
        assertEquals(Duration.ofMinutes(5), cacheKey.getExpire());
        assertTrue(cacheKey.getKey().contains("fingerprint.pwdVerified"));
        assertTrue(cacheKey.getKey().contains("12345"));
    }
}
