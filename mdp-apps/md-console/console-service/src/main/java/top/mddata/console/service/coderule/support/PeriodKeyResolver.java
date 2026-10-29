package top.mddata.console.service.coderule.support;

import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.enumeration.coderule.OffsetUnitEnum;
import top.mddata.console.enumeration.coderule.ResetRuleEnum;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;

/**
 * 流水号周期标签计算器。
 * 周期偏移只作用于本类（periodKey 生成），不影响日期明细的取值。
 */
public class PeriodKeyResolver {

    private PeriodKeyResolver() {
    }

    /**
     * 计算周期标签
     *
     * @param resetRule 归零规则
     * @param cycleOffset 周期偏移量（0=自然周期）
     * @param offsetUnit 偏移单位
     * @param bizTime 生成时刻
     * @return 周期标签
     */
    public static String resolve(ResetRuleEnum resetRule, Integer cycleOffset, OffsetUnitEnum offsetUnit, LocalDateTime bizTime) {
        ArgumentAssert.notNull(resetRule, "流水号规则必须配置归零规则");
        if (resetRule == ResetRuleEnum.NEVER) {
            return "FOREVER";
        }
        LocalDateTime baseTime = shift(bizTime, cycleOffset == null ? 0 : cycleOffset, offsetUnit);
        return switch (resetRule) {
            case DAY -> baseTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            case MONTH -> baseTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
            case YEAR -> baseTime.format(DateTimeFormatter.ofPattern("yyyy"));
            case WEEK -> baseTime.get(IsoFields.WEEK_BASED_YEAR) + "W"
                    + String.format("%02d", baseTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            default -> throw new IllegalArgumentException("不支持的归零规则: " + resetRule);
        };
    }

    private static LocalDateTime shift(LocalDateTime bizTime, int cycleOffset, OffsetUnitEnum offsetUnit) {
        if (cycleOffset == 0) {
            return bizTime;
        }
        // “N号切换”按自然日计：N号当天即归属新周期，故回退 N-1 天（如5号切换 → 回退4天）
        if (offsetUnit == OffsetUnitEnum.DAY) {
            return bizTime.minusDays((long) cycleOffset - 1);
        }
        // 小时偏移：早N点切换，回退 N 小时
        return bizTime.minusHours(cycleOffset);
    }
}
