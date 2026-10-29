package top.mddata.console.service.coderule.generator;

import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;

/**
 * 规则明细生成器（每个规则类别一个实现）。
 */
public interface RuleItemGenerator {

    /**
     * 本生成器负责的规则类别
     */
    CodeRuleTypeEnum getType();

    /**
     * 生成本明细段内容
     *
     * @param config 明细配置
     * @param context 生成上下文
     * @return 本段文本
     */
    String generate(CodeRuleItemConfig config, GenerateContext context);
}
