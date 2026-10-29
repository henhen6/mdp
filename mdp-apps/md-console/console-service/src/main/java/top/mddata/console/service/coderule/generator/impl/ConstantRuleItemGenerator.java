package top.mddata.console.service.coderule.generator.impl;

import org.springframework.stereotype.Component;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.service.coderule.generator.GenerateContext;
import top.mddata.console.service.coderule.generator.RuleItemGenerator;

/**
 * 常量生成器：输出固定值（按明细顺序充当单号的前缀/后缀/中间值）。
 */
@Component
public class ConstantRuleItemGenerator implements RuleItemGenerator {

    @Override
    public CodeRuleTypeEnum getType() {
        return CodeRuleTypeEnum.CONSTANT;
    }

    @Override
    public String generate(CodeRuleItemConfig config, GenerateContext context) {
        ArgumentAssert.notBlank(config.getValue(), "常量规则的固定值不能为空");
        return config.getValue();
    }
}
