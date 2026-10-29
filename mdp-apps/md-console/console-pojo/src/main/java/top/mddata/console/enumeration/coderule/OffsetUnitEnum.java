package top.mddata.console.enumeration.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 日期偏移单位
 * [HOUR-小时 DAY-天]
 */
@Getter
@AllArgsConstructor
@Schema(title = "OffsetUnitEnum", description = "编码规则-日期偏移单位")
public enum OffsetUnitEnum implements BaseEnum<String> {
    /** 小时 */
    HOUR("HOUR", "小时"),
    /** 天 */
    DAY("DAY", "天");
    private final String code;
    private final String desc;
}
