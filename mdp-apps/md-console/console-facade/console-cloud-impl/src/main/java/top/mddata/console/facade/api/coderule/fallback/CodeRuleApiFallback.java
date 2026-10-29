package top.mddata.console.facade.api.coderule.fallback;

import org.springframework.stereotype.Component;
import top.mddata.base.base.R;
import top.mddata.console.dto.coderule.CodeGenerateDto;
import top.mddata.console.dto.coderule.CodeGenerateMultiDto;
import top.mddata.console.facade.api.coderule.CodeRuleApi;

import java.util.List;

/**
 * 编码规则（单号生成）内部接口降级实现。
 */
@Component
public class CodeRuleApiFallback implements CodeRuleApi {

    @Override
    public R<String> generate(CodeGenerateDto dto) {
        return R.timeout();
    }

    @Override
    public R<List<String>> generateBatch(CodeGenerateDto dto) {
        return R.timeout();
    }

    @Override
    public R<List<String>> generateMulti(CodeGenerateMultiDto dto) {
        return R.timeout();
    }
}