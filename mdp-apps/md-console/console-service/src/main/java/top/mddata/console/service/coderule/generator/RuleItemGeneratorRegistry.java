package top.mddata.console.service.coderule.generator;

import org.springframework.stereotype.Component;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 规则明细生成器注册表（启动时收集所有实现）。
 */
@Component
public class RuleItemGeneratorRegistry {

    private final Map<CodeRuleTypeEnum, RuleItemGenerator> registry = new EnumMap<>(CodeRuleTypeEnum.class);

    public RuleItemGeneratorRegistry(List<RuleItemGenerator> generators) {
        for (RuleItemGenerator generator : generators) {
            registry.put(generator.getType(), generator);
        }
    }

    public RuleItemGenerator get(CodeRuleTypeEnum type) {
        RuleItemGenerator generator = registry.get(type);
        ArgumentAssert.notNull(generator, "不支持的规则类别[{}]", type);
        return generator;
    }
}
