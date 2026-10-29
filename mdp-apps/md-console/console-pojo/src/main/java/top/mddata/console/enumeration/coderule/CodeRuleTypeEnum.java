package top.mddata.console.enumeration.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 规则类别
 * [10-常量 20-日期 30-变量 40-流水号 50-随机码 60-校验位 70-GUID/雪花 80-组织编码 90-父单号截取]
 */
@Getter
@AllArgsConstructor
@Schema(title = "CodeRuleTypeEnum", description = "编码规则-规则类别")
public enum CodeRuleTypeEnum implements BaseEnum<Integer> {
    /** 常量 */
    CONSTANT(10, "常量"),
    /** 日期 */
    DATE(20, "日期"),
    /** 变量 */
    VARIABLE(30, "变量"),
    /** 流水号 */
    SERIAL(40, "流水号"),
    /** 随机码 */
    RANDOM(50, "随机码"),
    /** 校验位 */
    CHECK_DIGIT(60, "校验位"),
    /** GUID/雪花 */
    GUID(70, "GUID/雪花ID"),
    /** 组织编码 */
    ORG_CODE(80, "组织编码"),
    /** 父单号截取 */
    PARENT_CUT(90, "父单号截取");
    private final Integer code;
    private final String desc;

    /**
     * 按 code 查找枚举，不存在返回 null
     */
    public static CodeRuleTypeEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (CodeRuleTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }
}
