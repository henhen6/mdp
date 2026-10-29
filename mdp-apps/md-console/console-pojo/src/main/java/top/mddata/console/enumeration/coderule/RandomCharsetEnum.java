package top.mddata.console.enumeration.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 随机码字符集
 * [DIGIT-数字 UPPER-大写字母 LOWER-小写字母 DIGIT_UPPER-数字+大写 DIGIT_LOWER-数字+小写 UPPER_LOWER-大写+小写 ALL-数字+大小写]
 */
@Getter
@AllArgsConstructor
@Schema(title = "RandomCharsetEnum", description = "编码规则-随机码字符集")
public enum RandomCharsetEnum implements BaseEnum<String> {
    /** 数字 */
    DIGIT("DIGIT", "数字"),
    /** 大写字母 */
    UPPER("UPPER", "大写字母"),
    /** 小写字母 */
    LOWER("LOWER", "小写字母"),
    /** 数字+大写 */
    DIGIT_UPPER("DIGIT_UPPER", "数字+大写"),
    /** 数字+小写 */
    DIGIT_LOWER("DIGIT_LOWER", "数字+小写"),
    /** 大写+小写 */
    UPPER_LOWER("UPPER_LOWER", "大写+小写"),
    /** 数字+大小写 */
    ALL("ALL", "数字+大小写");
    private final String code;
    private final String desc;
}
