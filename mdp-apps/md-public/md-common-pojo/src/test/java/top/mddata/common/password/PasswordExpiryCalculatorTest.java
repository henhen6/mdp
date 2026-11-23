package top.mddata.common.password;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static top.mddata.common.password.PasswordExpiryCalculator.Status;

class PasswordExpiryCalculatorTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 23, 10, 0);

    @Test
    void shouldReturnNeverWhenExpireTimeIsNullOrMax() {
        assertNull(PasswordExpiryCalculator.evaluate(NOW, null, 7).remainDays());
        assertEquals(Status.NEVER,
                PasswordExpiryCalculator.evaluate(NOW, LocalDateTime.MAX, 7).status());
    }

    @Test
    void shouldExpireAtExactDeadline() {
        var result = PasswordExpiryCalculator.evaluate(NOW, NOW, 7);
        assertEquals(Status.EXPIRED, result.status());
        assertEquals(0L, result.remainDays());
    }

    @Test
    void shouldCalculateCalendarDaysAndThreshold() {
        var today = PasswordExpiryCalculator.evaluate(NOW, NOW.plusHours(2), 7);
        assertEquals(Status.EXPIRING, today.status());
        assertEquals(0L, today.remainDays());

        var boundary = PasswordExpiryCalculator.evaluate(NOW, NOW.plusDays(7), 7);
        assertEquals(Status.EXPIRING, boundary.status());
        assertEquals(7L, boundary.remainDays());

        assertEquals(Status.NORMAL,
                PasswordExpiryCalculator.evaluate(NOW, NOW.plusDays(8), 7).status());
        assertEquals(Status.NORMAL,
                PasswordExpiryCalculator.evaluate(NOW, NOW.plusDays(1), 0).status());
    }

    @Test
    void shouldRejectNegativeReminderDays() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordExpiryCalculator.evaluate(NOW, NOW.plusDays(1), -1));
    }

    @Test
    void renewExpireTime_旧值为null_返回null表示豁免() {
        assertNull(PasswordExpiryCalculator.renewExpireTime(null, "3M"));
    }

    @Test
    void renewExpireTime_旧值有值_按周期重算() {
        // 该用例曾因 DateUtils "3M" 被解析为 3 分钟的缺陷临时改用 "1y"，修复大小写处理后恢复简报原文
        LocalDateTime old = LocalDateTime.now().plusDays(1);
        LocalDateTime renewed = PasswordExpiryCalculator.renewExpireTime(old, "3M");
        assertNotNull(renewed);
        assertTrue(renewed.isAfter(old));
    }

    @Test
    void renewExpireTime_MAX不豁免_重算为有期限() {
        LocalDateTime renewed = PasswordExpiryCalculator.renewExpireTime(LocalDateTime.MAX, "3M");
        assertNotNull(renewed);
        assertTrue(renewed.isBefore(LocalDateTime.MAX));
    }

    @Test
    void renewExpireTime_配置为空_重算为MAX永不过期() {
        LocalDateTime old = LocalDateTime.now().plusDays(1);
        assertEquals(LocalDateTime.MAX, PasswordExpiryCalculator.renewExpireTime(old, ""));
    }
}
