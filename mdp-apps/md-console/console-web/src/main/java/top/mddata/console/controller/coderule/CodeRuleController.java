package top.mddata.console.controller.coderule;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.console.dto.coderule.CodeRuleDto;
import top.mddata.console.dto.coderule.CodeRuleTestDto;
import top.mddata.console.entity.coderule.CodeRule;
import top.mddata.console.query.coderule.CodeRuleQuery;
import top.mddata.console.service.coderule.CodeRuleService;
import top.mddata.console.vo.coderule.CodeRuleVo;

import java.util.List;
import java.util.Map;

/**
 * 编码规则 控制层。
 *
 * @author henhen
 * @since 2026/10/29
 */
@RestController
@Validated
@Tag(name = "编码规则")
@RequestMapping("/system/codeRule")
@RequiredArgsConstructor
public class CodeRuleController extends SuperController<CodeRuleService, CodeRule> {

    /**
     * 添加编码规则。
     *
     * @param dto 编码规则
     * @return 新增后的主键
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存编码规则")
    @RequestLog(value = "新增", logType = RequestLog.LogType.ADD, request = false)
    public R<Long> save(@Validated @RequestBody CodeRuleDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除编码规则（级联删除明细）。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除编码规则")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新编码规则。
     *
     * @param dto 编码规则
     * @return 更新后的主键
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新编码规则")
    @RequestLog(value = "修改", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody CodeRuleDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 分页查询编码规则。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询编码规则")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<CodeRuleVo>> page(@RequestBody @Validated PageParams<CodeRuleQuery> params) {
        Page<CodeRuleVo> page = Page.of(params.getCurrent(), params.getSize());
        CodeRule entity = BeanUtil.toBean(params.getModel(), CodeRule.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, CodeRuleVo.class);
        return R.success(page);
    }

    /**
     * 根据编码规则主键获取详细信息。
     *
     * @param id 编码规则主键
     * @return 编码规则详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取编码规则")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<CodeRuleVo> get(@RequestParam Long id) {
        CodeRule entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, CodeRuleVo.class));
    }

    /**
     * 试生成（预览模式：流水号段不真实递增，仅模拟展示）
     */
    @PostMapping("/testGenerate")
    @Operation(summary = "试生成", description = "按规则预览生成单号（不占用流水号）")
    @RequestLog(value = "试生成编码规则", logType = RequestLog.LogType.QUERY)
    public R<List<String>> testGenerate(@Validated @RequestBody CodeRuleTestDto dto) {
        return R.success(superService.testGenerate(dto.getRuleCode(), dto.getParams(), dto.getCount()));
    }

    /**
     * 水位校准：将流水号 Redis 当前值落库到水位表（用于水位记录被误删后的恢复，无需手工 INSERT）
     */
    @PostMapping("/flushSn")
    @Operation(summary = "水位校准", description = "将流水号 Redis 当前值落库到水位表")
    @RequestLog(value = "'编码规则水位校准:' + #ruleCode", logType = RequestLog.LogType.UPDATE)
    public R<Map<Long, Long>> flushSn(@NotBlank @RequestParam String ruleCode) {
        return R.success(superService.flushSn(ruleCode));
    }

}