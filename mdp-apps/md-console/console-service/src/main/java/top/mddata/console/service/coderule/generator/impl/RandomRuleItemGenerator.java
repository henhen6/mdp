package top.mddata.console.service.coderule.generator.impl;

import cn.hutool.core.util.RandomUtil;
import org.springframework.stereotype.Component;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.service.coderule.generator.GenerateContext;
import top.mddata.console.service.coderule.generator.RuleItemGenerator;

/**
 * 随机码生成器：按字符集生成指定长度随机串（防伪单号、邀请码等场景）。
 */
@Component
public class RandomRuleItemGenerator implements RuleItemGenerator {

    private static final String DIGIT = "0123456789";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";

    @Override
    public CodeRuleTypeEnum getType() {
        return CodeRuleTypeEnum.RANDOM;
    }

    @Override
    public String generate(CodeRuleItemConfig config, GenerateContext context) {
        ArgumentAssert.notNull(config.getLength(), "随机码规则必须配置长度");
        String charset = switch (config.getCharset() == null ? "" : config.getCharset()) {
            case "DIGIT" -> DIGIT;
            case "UPPER" -> UPPER;
            case "LOWER" -> LOWER;
            case "DIGIT_UPPER" -> DIGIT + UPPER;
            case "DIGIT_LOWER" -> DIGIT + LOWER;
            case "UPPER_LOWER" -> UPPER + LOWER;
            default -> DIGIT + UPPER + LOWER;
        };
        return RandomUtil.randomString(charset, config.getLength());
    }
}
