package top.mddata.console.enumeration.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 流水号溢出策略
 * [ERROR-报错 GROW-超长递增]
 */
@Getter
@AllArgsConstructor
@Schema(title = "OverflowStrategyEnum", description = "编码规则-流水号溢出策略")
public enum OverflowStrategyEnum implements BaseEnum<String> {
    /** 报错 */
    ERROR("ERROR", "报错"),
    /** 超长递增 */
    GROW("GROW", "超长递增");
    private final String code;
    private final String desc;
}
