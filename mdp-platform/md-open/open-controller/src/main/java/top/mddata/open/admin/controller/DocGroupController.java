package top.mddata.open.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.open.admin.dto.DocGroupDto;
import top.mddata.open.admin.entity.DocGroup;
import top.mddata.open.admin.query.DocGroupQuery;
import top.mddata.open.admin.service.DocGroupService;
import top.mddata.open.admin.vo.DocGroupVo;

import java.util.List;

/**
 * 文档分组 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@RestController
@Validated
@Tag(name = "文档分组")
@RequestMapping("/admin/docGroup")
@RequiredArgsConstructor
public class DocGroupController extends SuperController<DocGroupService, DocGroup> {
    /**
     * 添加文档分组。
     *
     * @param dto 文档分组
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存文档分组")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody DocGroupDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除文档分组。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除文档分组")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }


    /**
     * 批量查询
     * @param params 查询参数
     * @return 集合
     */
    @PostMapping("/list")
    @Operation(summary = "批量查询", description = "批量查询")
    @RequestLog(value = "批量查询", response = false)
    public R<List<DocGroupVo>> list(@RequestBody @Validated DocGroupQuery params) {
        DocGroup entity = BeanUtil.toBean(params, DocGroup.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<DocGroupVo> listVo = superService.listAs(wrapper, DocGroupVo.class);
        return R.success(listVo);
    }
}
