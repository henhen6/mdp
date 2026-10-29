package top.mddata.console.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.console.dto.coderule.CodeGenerateDto;
import top.mddata.console.dto.coderule.CodeGenerateMultiDto;
import top.mddata.console.service.coderule.CodeRuleService;

import java.util.List;

/**
 * 内部-编码规则（单号生成）入口：仅供平台内部服务经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-编码规则")
@RequestMapping("/inner/codeRule")
@RequiredArgsConstructor
public class InnerCodeRuleController {

    private final CodeRuleService codeRuleService;

    /**
     * 生成单个单号
     */
    @Operation(hidden = true)
    @PostMapping("/generate")
    public R<String> generate(@Validated @RequestBody CodeGenerateDto dto) {
        return R.success(codeRuleService.generate(dto.getRuleCode(), dto.getParams()));
    }

    /**
     * 同参批量生成
     */
    @Operation(hidden = true)
    @PostMapping("/generateBatch")
    public R<List<String>> generateBatch(@Validated @RequestBody CodeGenerateDto dto) {
        return R.success(codeRuleService.generateBatch(dto.getRuleCode(), dto.getParams(), dto.getCount()));
    }

    /**
     * 逐参批量生成
     */
    @Operation(hidden = true)
    @PostMapping("/generateMulti")
    public R<List<String>> generateMulti(@Validated @RequestBody CodeGenerateMultiDto dto) {
        return R.success(codeRuleService.generateMulti(dto.getRuleCode(), dto.getParamsList()));
    }
}