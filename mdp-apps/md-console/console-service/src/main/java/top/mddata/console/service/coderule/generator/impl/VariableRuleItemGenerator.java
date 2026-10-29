package top.mddata.console.service.coderule.generator.impl;

import org.springframework.stereotype.Component;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.enumeration.coderule.VarTypeEnum;
import top.mddata.console.service.coderule.generator.GenerateContext;
import top.mddata.console.service.coderule.generator.RuleItemGenerator;

import java.util.Map;

/**
 * 变量生成器：取调用方 map 中的值。
 * 替换（REPLACE）：传啥显示啥；映射（MAPPING）：key→value 映射表翻译，key 无映射时显示 key 本身兜底。
 */
@Component
public class VariableRuleItemGenerator implements RuleItemGenerator {

    @Override
    public CodeRuleTypeEnum getType() {
        return CodeRuleTypeEnum.VARIABLE;
    }

    @Override
    public String generate(CodeRuleItemConfig config, GenerateContext context) {
        ArgumentAssert.notBlank(config.getVarName(), "变量规则必须配置变量名");
        String value = context.getParams().get(config.getVarName());
        ArgumentAssert.notNull(value, "缺少变量[{}]", config.getVarName());
        if (VarTypeEnum.MAPPING.getCode().equals(config.getVarType())) {
            Map<String, String> mappings = config.getMappings();
            // key 无映射时用 key 本身兜底显示
            return mappings != null ? mappings.getOrDefault(value, value) : value;
        }
        return value;
    }
}
