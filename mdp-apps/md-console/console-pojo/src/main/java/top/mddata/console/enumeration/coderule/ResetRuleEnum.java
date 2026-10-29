package top.mddata.console.enumeration.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 流水号归零规则
 * [NEVER-不归零 YEAR-按年 MONTH-按月 WEEK-按周 DAY-按日]
 */
@Getter
@AllArgsConstructor
@Schema(title = "ResetRuleEnum", description = "编码规则-流水号归零规则")
public enum ResetRuleEnum implements BaseEnum<String> {
    /** 不归零 */
    NEVER("NEVER", "不归零"),
    /** 按年 */
    YEAR("YEAR", "按年"),
    /** 按月 */
    MONTH("MONTH", "按月"),
    /** 按周 */
    WEEK("WEEK", "按周"),
    /** 按日 */
    DAY("DAY", "按日");
    private final String code;
    private final String desc;
}
