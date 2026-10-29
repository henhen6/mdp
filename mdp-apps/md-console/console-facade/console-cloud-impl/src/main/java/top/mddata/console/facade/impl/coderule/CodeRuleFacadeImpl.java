package top.mddata.console.facade.impl.coderule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.base.exception.ArgumentException;
import top.mddata.console.dto.coderule.CodeGenerateDto;
import top.mddata.console.dto.coderule.CodeGenerateMultiDto;
import top.mddata.console.facade.api.coderule.CodeRuleApi;
import top.mddata.console.facade.coderule.CodeRuleFacade;

import java.util.List;
import java.util.Map;

/**
 * 编码规则（单号生成）门面-微服务版实现：经 Feign 调用内部接口。
 */
@Service
@RequiredArgsConstructor
public class CodeRuleFacadeImpl implements CodeRuleFacade {

    private final CodeRuleApi codeRuleApi;

    @Override
    public String generate(String ruleCode, Map<String, String> params) {
        CodeGenerateDto dto = new CodeGenerateDto().setRuleCode(ruleCode).setParams(params);
        R<String> result = codeRuleApi.generate(dto);
        if (result == null || !result.getIsSuccess()) {
            throw new ArgumentException("生成单号失败: {}",
                    result != null ? result.getMsg() : "Feign调用失败");
        }
        return result.getData();
    }

    @Override
    public List<String> generateBatch(String ruleCode, Map<String, String> params, int count) {
        CodeGenerateDto dto = new CodeGenerateDto().setRuleCode(ruleCode).setParams(params).setCount(count);
        R<List<String>> result = codeRuleApi.generateBatch(dto);
        if (result == null || !result.getIsSuccess()) {
            throw new ArgumentException("批量生成单号失败: {}",
                    result != null ? result.getMsg() : "Feign调用失败");
        }
        return result.getData();
    }

    @Override
    public List<String> generateMulti(String ruleCode, List<Map<String, String>> paramsList) {
        CodeGenerateMultiDto dto = new CodeGenerateMultiDto().setRuleCode(ruleCode).setParamsList(paramsList);
        R<List<String>> result = codeRuleApi.generateMulti(dto);
        if (result == null || !result.getIsSuccess()) {
            throw new ArgumentException("多参数生成单号失败: {}",
                    result != null ? result.getMsg() : "Feign调用失败");
        }
        return result.getData();
    }
}
