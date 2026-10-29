package top.mddata.console.service.coderule.generator.impl;

import org.springframework.stereotype.Component;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.service.coderule.generator.GenerateContext;
import top.mddata.console.service.coderule.generator.RuleItemGenerator;

import java.time.format.DateTimeFormatter;

/**
 * 日期生成器：按配置的格式占位符实时生成（取生成时刻，不随流水号周期偏移）。
 */
@Component
public class DateRuleItemGenerator implements RuleItemGenerator {

    @Override
    public CodeRuleTypeEnum getType() {
        return CodeRuleTypeEnum.DATE;
    }

    @Override
    public String generate(CodeRuleItemConfig config, GenerateContext context) {
        ArgumentAssert.notBlank(config.getPattern(), "日期规则的格式不能为空");
        try {
            return context.getBizTime().format(DateTimeFormatter.ofPattern(config.getPattern()));
        } catch (IllegalArgumentException e) {
            throw new ArgumentException("日期格式[{}]不合法：{}", config.getPattern(), e.getMessage());
        }
    }
}
