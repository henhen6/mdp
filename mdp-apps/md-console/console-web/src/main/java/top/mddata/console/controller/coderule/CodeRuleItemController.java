package top.mddata.console.controller.coderule;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.console.dto.coderule.CodeRuleItemSaveDto;
import top.mddata.console.entity.coderule.CodeRuleItem;
import top.mddata.console.service.coderule.CodeRuleService;
import top.mddata.console.vo.coderule.CodeRuleItemVo;

import java.util.List;

/**
 * 编码规则明细 控制层。
 *
 * @author henhen
 * @since 2026/10/29
 */
@RestController
@Validated
@Tag(name = "编码规则明细")
@RequestMapping("/system/codeRuleItem")
@RequiredArgsConstructor
public class CodeRuleItemController {

    private final CodeRuleService codeRuleService;

    /**
     * 查询规则的明细列表（按 sort 排序）。
     *
     * @param ruleId 规则ID
     * @return 明细列表
     */
    @GetMapping("/listByRuleId")
    @Operation(summary = "查询规则明细", description = "根据规则ID查询明细列表（按 sort 排序）")
    @RequestLog(value = "'查询规则明细:' + #ruleId", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<CodeRuleItemVo>> listByRuleId(@RequestParam Long ruleId) {
        List<CodeRuleItem> items = codeRuleService.listItemsByRuleId(ruleId);
        List<CodeRuleItemVo> vos = items.stream()
                .map(item -> BeanUtil.toBean(item, CodeRuleItemVo.class))
                .toList();
        return R.success(vos);
    }

    /**
     * 全量保存明细（前端拖拽排序后整组提交，按列表顺序重排 sort，全删全插）。
     *
     * @param dto 保存入参
     * @return {@code true} 保存成功
     */
    @PostMapping("/saveAll")
    @Operation(summary = "全量保存明细", description = "整组提交规则明细（全量替换，按列表顺序重排 sort）")
    @RequestLog(value = "保存规则明细", logType = RequestLog.LogType.ADD, request = false)
    public R<Boolean> saveAll(@Validated @RequestBody CodeRuleItemSaveDto dto) {
        List<CodeRuleItem> items = dto.getItems().stream()
                .map(item -> BeanUtil.toBean(item, CodeRuleItem.class))
                .toList();
        codeRuleService.saveItems(dto.getRuleId(), items);
        return R.success(true);
    }

}