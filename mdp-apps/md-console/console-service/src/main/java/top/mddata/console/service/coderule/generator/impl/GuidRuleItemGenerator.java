package top.mddata.console.service.coderule.generator.impl;

import cn.hutool.core.util.IdUtil;
import com.baidu.fsg.uid.UidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.enumeration.coderule.GuidTypeEnum;
import top.mddata.console.service.coderule.generator.GenerateContext;
import top.mddata.console.service.coderule.generator.RuleItemGenerator;

/**
 * GUID/雪花ID 生成器：全局唯一标识，length>0 时截取前 N 位。
 */
@Component
@RequiredArgsConstructor
public class GuidRuleItemGenerator implements RuleItemGenerator {

    private final UidGenerator uidGenerator;

    @Override
    public CodeRuleTypeEnum getType() {
        return CodeRuleTypeEnum.GUID;
    }

    @Override
    public String generate(CodeRuleItemConfig config, GenerateContext context) {
        String raw;
        if (GuidTypeEnum.SNOWFLAKE.getCode().equals(config.getGuidType())) {
            raw = String.valueOf(uidGenerator.getUid());
        } else {
            raw = IdUtil.fastSimpleUUID();
        }
        Integer length = config.getLength();
        return (length != null && length > 0 && raw.length() > length) ? raw.substring(0, length) : raw;
    }
}
