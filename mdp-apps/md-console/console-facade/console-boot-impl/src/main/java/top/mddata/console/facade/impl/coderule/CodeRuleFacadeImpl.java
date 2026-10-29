package top.mddata.console.facade.impl.coderule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.console.facade.coderule.CodeRuleFacade;
import top.mddata.console.service.coderule.CodeRuleService;

import java.util.List;
import java.util.Map;

/**
 * 编码规则（单号生成）门面-单体版实现：本地直调服务层。
 */
@Service
@RequiredArgsConstructor
public class CodeRuleFacadeImpl implements CodeRuleFacade {

    private final CodeRuleService codeRuleService;

    @Override
    public String generate(String ruleCode, Map<String, String> params) {
        return codeRuleService.generate(ruleCode, params);
    }

    @Override
    public List<String> generateBatch(String ruleCode, Map<String, String> params, int count) {
        return codeRuleService.generateBatch(ruleCode, params, count);
    }

    @Override
    public List<String> generateMulti(String ruleCode, List<Map<String, String>> paramsList) {
        return codeRuleService.generateMulti(ruleCode, paramsList);
    }
}