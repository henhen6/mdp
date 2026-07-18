package top.mddata.open.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
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
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.open.dto.admin.HelpDocDto;
import top.mddata.open.entity.admin.HelpDoc;
import top.mddata.open.query.admin.HelpDocQuery;
import top.mddata.open.service.admin.HelpDocService;
import top.mddata.open.vo.admin.HelpDocVo;

import java.util.List;

/**
 * 帮助文档 控制层。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@RestController
@Validated
@Tag(name = "帮助文档")
@RequestMapping("/admin/helpDoc")
@RequiredArgsConstructor
public class HelpDocController extends SuperController<HelpDocService, HelpDoc> {
    /**
     * 添加帮助文档。
     *
     * @param dto 帮助文档
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存帮助文档")
    @RequestLog(value = "新增", logType = RequestLog.LogType.ADD, request = false)
    public R<Long> save(@Validated @RequestBody HelpDocDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除帮助文档。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除帮助文档")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新帮助文档。
     *
     * @param dto 帮助文档
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新帮助文档")
    @RequestLog(value = "修改", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody HelpDocDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据帮助文档主键获取详细信息。
     *
     * @param id 帮助文档主键
     * @return 帮助文档详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取帮助文档")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<HelpDocVo> get(@RequestParam Long id) {
        HelpDoc entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, HelpDocVo.class));
    }

    @Operation(summary = "调整节点", description = "调整节点")
    @PostMapping("/move")
    @RequestLog(value = "调整节点", logType = RequestLog.LogType.UPDATE)
    public R<Boolean> move(@RequestParam Long sourceId, @RequestParam(required = false) Long targetId) {
        superService.move(sourceId, targetId);
        return R.success();
    }

    /**
     * 按树结构查询
     *
     * @param params 查询参数
     * @return 查询结果
     */
    @Operation(summary = "按树结构查询")
    @PostMapping("/tree")
    @RequestLog(value = "按树结构查询", logType = RequestLog.LogType.QUERY)
    public R<List<HelpDocVo>> tree(@RequestBody @Validated HelpDocQuery params) {
        HelpDoc entity = BeanUtil.toBean(params, HelpDoc.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<HelpDocVo> listVo = superService.listAs(wrapper, HelpDocVo.class);
        return R.success(MyTreeUtil.buildTreeEntity(listVo, HelpDocVo::new));
    }
}
