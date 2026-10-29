package top.mddata.console.service.coderule;

import org.junit.jupiter.api.Test;
import top.mddata.console.enumeration.coderule.OffsetUnitEnum;
import top.mddata.console.enumeration.coderule.ResetRuleEnum;
import top.mddata.console.service.coderule.support.PeriodKeyResolver;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeriodKeyResolverTest {

    @Test
    void 按日归零() {
        assertEquals("20261028", PeriodKeyResolver.resolve(ResetRuleEnum.DAY, 0, OffsetUnitEnum.HOUR,
                LocalDateTime.of(2026, 10, 28, 15, 30, 0)));
    }

    @Test
    void 按月按年按周() {
        LocalDateTime t = LocalDateTime.of(2026, 10, 28, 15, 30, 0);
        assertEquals("202610", PeriodKeyResolver.resolve(ResetRuleEnum.MONTH, 0, OffsetUnitEnum.HOUR, t));
        assertEquals("2026", PeriodKeyResolver.resolve(ResetRuleEnum.YEAR, 0, OffsetUnitEnum.HOUR, t));
        // 2026-10-28 是周三，ISO 周 = 2026W44
        assertEquals("2026W44", PeriodKeyResolver.resolve(ResetRuleEnum.WEEK, 0, OffsetUnitEnum.HOUR, t));
    }

    @Test
    void 不归零() {
        assertEquals("FOREVER", PeriodKeyResolver.resolve(ResetRuleEnum.NEVER, 0, OffsetUnitEnum.HOUR,
                LocalDateTime.of(2026, 10, 28, 15, 30, 0)));
    }

    @Test
    void 周期偏移_业务日早8点切换() {
        // 偏移只作用于周期标签：生成时间 -8h 后再格式化
        // 凌晨 1:30 归前一业务日
        assertEquals("20261027", PeriodKeyResolver.resolve(ResetRuleEnum.DAY, 8, OffsetUnitEnum.HOUR,
                LocalDateTime.of(2026, 10, 28, 1, 30, 0)));
        // 早 8 点后归新周期
        assertEquals("20261028", PeriodKeyResolver.resolve(ResetRuleEnum.DAY, 8, OffsetUnitEnum.HOUR,
                LocalDateTime.of(2026, 10, 28, 8, 0, 1)));
    }

    @Test
    void 周期偏移_财务月每月5号切换() {
        // resetRule=MONTH + 偏移 5 天
        assertEquals("202609", PeriodKeyResolver.resolve(ResetRuleEnum.MONTH, 5, OffsetUnitEnum.DAY,
                LocalDateTime.of(2026, 10, 3, 0, 0, 0)));
        assertEquals("202610", PeriodKeyResolver.resolve(ResetRuleEnum.MONTH, 5, OffsetUnitEnum.DAY,
                LocalDateTime.of(2026, 10, 5, 0, 0, 0)));
    }
}
