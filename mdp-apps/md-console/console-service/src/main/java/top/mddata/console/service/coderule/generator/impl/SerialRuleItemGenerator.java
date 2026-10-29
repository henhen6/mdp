package top.mddata.console.service.coderule.generator.impl;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.enumeration.coderule.OverflowStrategyEnum;
import top.mddata.console.service.coderule.generator.GenerateContext;
import top.mddata.console.service.coderule.generator.RuleItemGenerator;

/**
 * 流水号生成器：从上下文取预分配值（引擎批量时已用 INCRBY 一次原子分配连续区间），按长度补位/超长处理。
 */
@Component
public class SerialRuleItemGenerator implements RuleItemGenerator {

    @Override
    public CodeRuleTypeEnum getType() {
        return CodeRuleTypeEnum.SERIAL;
    }

    @Override
    public String generate(CodeRuleItemConfig config, GenerateContext context) {
        Long value = context.nextSerial();
        ArgumentAssert.notNull(value, "流水号分配失败");
        return padAndCheck(value, config);
    }

    /**
     * 补位与超长处理：数字位数 < length 时按 padChar（默认 0）左补位；超过 length 时按超长策略报错或原样输出
     */
    public static String padAndCheck(long value, CodeRuleItemConfig config) {
        String raw = String.valueOf(value);
        int length = config.getLength() == null ? 0 : config.getLength();
        if (length <= 0 || raw.length() == length) {
            return raw;
        }
        if (raw.length() < length) {
            char pad = StrUtil.isBlank(config.getPadChar()) ? '0' : config.getPadChar().charAt(0);
            return StrUtil.padPre(raw, length, pad);
        }
        if (OverflowStrategyEnum.GROW.getCode().equals(config.getOverflowStrategy())) {
            return raw;
        }
        throw new ArgumentException("流水号[{}]已超过配置长度[{}]，当前策略为报错", raw, length);
    }
}
