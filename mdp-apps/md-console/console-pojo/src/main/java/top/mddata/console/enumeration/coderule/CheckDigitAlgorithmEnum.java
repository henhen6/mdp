package top.mddata.console.enumeration.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 校验位算法
 * [LUHN-Luhn（仅纯数字前文） LUHN_MOD_N-Luhn Mod N（字母数字前文）]
 */
@Getter
@AllArgsConstructor
@Schema(title = "CheckDigitAlgorithmEnum", description = "编码规则-校验位算法")
public enum CheckDigitAlgorithmEnum implements BaseEnum<String> {
    /** Luhn（仅纯数字前文） */
    LUHN("LUHN", "Luhn（仅纯数字前文）"),
    /** Luhn Mod N（字母数字前文） */
    LUHN_MOD_N("LUHN_MOD_N", "Luhn Mod N（字母数字前文）");
    private final String code;
    private final String desc;
}
