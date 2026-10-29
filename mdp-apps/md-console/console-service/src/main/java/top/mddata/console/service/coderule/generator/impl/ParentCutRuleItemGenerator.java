package top.mddata.console.service.coderule.generator.impl;

import org.springframework.stereotype.Component;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.enumeration.coderule.CutTypeEnum;
import top.mddata.console.service.coderule.generator.GenerateContext;
import top.mddata.console.service.coderule.generator.RuleItemGenerator;

/**
 * 父单号截取生成器：从调用方 map 取父单号并截取复用。
 * INDEX：按下标截取（前缀定长）；SEPARATOR：取第 N 个分隔符之后的全部（支持负数从后往前数）。
 */
@Component
public class ParentCutRuleItemGenerator implements RuleItemGenerator {

    @Override
    public CodeRuleTypeEnum getType() {
        return CodeRuleTypeEnum.PARENT_CUT;
    }

    @Override
    public String generate(CodeRuleItemConfig config, GenerateContext context) {
        ArgumentAssert.notBlank(config.getVarName(), "父单号截取规则必须配置变量名");
        String parentCode = context.getParams().get(config.getVarName());
        ArgumentAssert.notNull(parentCode, "缺少变量[{}]", config.getVarName());
        if (CutTypeEnum.SEPARATOR.getCode().equals(config.getCutType())) {
            return cutBySeparator(parentCode, config);
        }
        return cutByIndex(parentCode, config);
    }

    private String cutByIndex(String parentCode, CodeRuleItemConfig config) {
        int startIndex = config.getStartIndex() == null ? 0 : config.getStartIndex();
        if (startIndex < 0 || startIndex > parentCode.length()) {
            throw new ArgumentException("父单号[{}]长度不足，无法从下标[{}]截取", parentCode, startIndex);
        }
        Integer length = config.getLength();
        int end = (length == null || length <= 0) ? parentCode.length()
                : Math.min(startIndex + length, parentCode.length());
        return parentCode.substring(startIndex, end);
    }

    /**
     * 取第 splitPosition 个分隔符之后的全部内容；负数从后往前数（-1=最后一个分隔符）
     */
    private String cutBySeparator(String parentCode, CodeRuleItemConfig config) {
        String separator = config.getSeparator();
        ArgumentAssert.notBlank(separator, "父单号截取规则必须配置分隔符");
        int splitPosition = config.getSplitPosition() == null ? 1 : config.getSplitPosition();
        if (splitPosition == 0) {
            throw new ArgumentException("分隔符位置不能为 0");
        }
        int fromIndex = 0;
        if (splitPosition > 0) {
            int pos = -1;
            for (int i = 0; i < splitPosition; i++) {
                pos = parentCode.indexOf(separator, pos + 1);
                if (pos < 0) {
                    throw new ArgumentException("父单号[{}]中不存在第[{}]个分隔符[{}]", parentCode, splitPosition, separator);
                }
            }
            fromIndex = pos + separator.length();
        } else {
            int pos = parentCode.length();
            for (int i = 0; i < -splitPosition; i++) {
                pos = parentCode.lastIndexOf(separator, pos - 1);
                if (pos < 0) {
                    throw new ArgumentException("父单号[{}]中不存在倒数第[{}]个分隔符[{}]", parentCode, -splitPosition, separator);
                }
            }
            fromIndex = pos + separator.length();
        }
        return parentCode.substring(fromIndex);
    }
}
