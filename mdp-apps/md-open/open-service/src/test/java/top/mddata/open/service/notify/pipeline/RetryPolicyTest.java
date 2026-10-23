package top.mddata.open.service.notify.pipeline;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryPolicyTest {

    @Test
    void 按梯度计算下次重试时间() {
        RetryPolicy policy = new RetryPolicy("2m,5m,30m,6h,12h");
        LocalDateTime now = LocalDateTime.of(2026, 10, 13, 10, 30, 45);
        // 时间被截断到分钟，再叠加梯度
        assertEquals(LocalDateTime.of(2026, 10, 13, 10, 32, 0), policy.nextRequestTime(1, now));
        assertEquals(LocalDateTime.of(2026, 10, 13, 10, 35, 0), policy.nextRequestTime(2, now));
        assertEquals(LocalDateTime.of(2026, 10, 13, 16, 30, 0), policy.nextRequestTime(4, now));
        // 最大投递次数 = 梯度级数：第 5 次失败后即用尽（与旧 buildNextSendTime 边界一致）
        assertNull(policy.nextRequestTime(5, now));
    }

    @Test
    void 次数用尽返回null() {
        RetryPolicy policy = new RetryPolicy("2m,5m");
        // 梯度共 2 级：第 2 次失败后即用尽
        assertNull(policy.nextRequestTime(2, LocalDateTime.now()));
        assertNull(policy.nextRequestTime(3, LocalDateTime.now()));
        assertEquals(2, policy.getMaxRetry());
    }

    @Test
    void 非法配置启动即失败() {
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(""));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(null));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy("2x"));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy("m"));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy("abc m"));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy("-5m"));
    }
}
