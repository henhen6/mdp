package top.mddata.console.service.coderule.generator.impl;

import org.springframework.stereotype.Component;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.enumeration.coderule.CheckDigitAlgorithmEnum;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.service.coderule.generator.GenerateContext;
import top.mddata.console.service.coderule.generator.RuleItemGenerator;

/**
 * 校验位生成器：基于已生成前文计算校验位（Luhn 仅支持纯数字前文，含字母自动用 Luhn Mod N）。
 * 防伪造、防录入错误。
 */
@Component
public class CheckDigitRuleItemGenerator implements RuleItemGenerator {

    private static final String LUHN_MOD_N_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public CodeRuleTypeEnum getType() {
        return CodeRuleTypeEnum.CHECK_DIGIT;
    }

    @Override
    public String generate(CodeRuleItemConfig config, GenerateContext context) {
        String prefix = context.getPrefix().toString();
        ArgumentAssert.notBlank(prefix, "校验位规则不能作为第一段（无前文可校验）");
        boolean allDigit = prefix.chars().allMatch(Character::isDigit);
        if (allDigit && !CheckDigitAlgorithmEnum.LUHN_MOD_N.getCode().equals(config.getAlgorithm())) {
            return String.valueOf(luhn(prefix));
        }
        return String.valueOf(luhnModN(prefix));
    }

    /**
     * 标准 Luhn 校验位（纯数字）
     */
    static int luhn(String digits) {
        int sum = 0;
        boolean doubleIt = true;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleIt) {
                d = d * 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return (10 - sum % 10) % 10;
    }

    /**
     * Luhn Mod N（字母数字，字母按 A=10 起映射）
     */
    static char luhnModN(String text) {
        int n = LUHN_MOD_N_CHARS.length();
        int sum = 0;
        boolean doubleIt = true;
        for (int i = text.length() - 1; i >= 0; i--) {
            int v = LUHN_MOD_N_CHARS.indexOf(Character.toUpperCase(text.charAt(i)));
            if (v < 0) {
                continue;
            }
            if (doubleIt) {
                v *= 2;
                if (v >= n) {
                    v = v - n + 1;
                }
            }
            sum += v;
            doubleIt = !doubleIt;
        }
        int check = (n - sum % n) % n;
        return LUHN_MOD_N_CHARS.charAt(check);
    }
}
