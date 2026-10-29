package top.mddata.console.facade.api.coderule;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.console.dto.coderule.CodeGenerateDto;
import top.mddata.console.dto.coderule.CodeGenerateMultiDto;
import top.mddata.console.facade.api.coderule.fallback.CodeRuleApiFallback;

import java.util.List;

/**
 * 编码规则（单号生成）内部接口。
 */
@FeignClient(name = AppConstants.CONSOLE_SERVER, fallback = CodeRuleApiFallback.class, path = "/inner/codeRule")
public interface CodeRuleApi {

    /**
     * 生成单个单号
     */
    @PostMapping("/generate")
    R<String> generate(@RequestBody CodeGenerateDto dto);

    /**
     * 同参批量生成
     */
    @PostMapping("/generateBatch")
    R<List<String>> generateBatch(@RequestBody CodeGenerateDto dto);

    /**
     * 逐参批量生成
     */
    @PostMapping("/generateMulti")
    R<List<String>> generateMulti(@RequestBody CodeGenerateMultiDto dto);
}