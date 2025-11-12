package top.mddata.console.organization.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.common.entity.Org;
import top.mddata.console.organization.dto.OrgDto;
import top.mddata.console.organization.query.OrgQuery;
import top.mddata.console.organization.service.OrgService;
import top.mddata.console.organization.vo.OrgVo;

import java.util.List;

/**
 * 组织 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 15:49:10
 */
@RestController
@Validated
@Tag(name = "组织")
@RequestMapping("/organization/org")
@RequiredArgsConstructor
public class OrgController extends SuperController<OrgService, Org> {
    private final OrgService orgService;

    /**
     * 添加组织。
     *
     * @param dto 组织
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存组织")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody OrgDto dto) {
        return R.success(orgService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除组织。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除组织")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(orgService.removeByIds(ids));
    }

    /**
     * 根据主键更新组织。
     *
     * @param dto 组织
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新组织")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody OrgDto dto) {
        return R.success(orgService.updateDtoById(dto).getId());
    }

    /**
     * 根据组织主键获取详细信息。
     *
     * @param id 组织主键
     * @return 组织详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取组织")
    @RequestLog("'单体查询:' + #id")
    public R<OrgVo> get(@RequestParam Long id) {
        Org entity = orgService.getById(id);
        return R.success(BeanUtil.toBean(entity, OrgVo.class));
    }

    /**
     * 分页查询组织。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询组织")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<OrgVo>> page(@RequestBody @Validated PageParams<OrgQuery> params) {
        Page<OrgVo> page = Page.of(params.getCurrent(), params.getSize());
        Org entity = BeanUtil.toBean(params.getModel(), Org.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        orgService.pageAs(page, wrapper, OrgVo.class);
        return R.success(page);
    }

    /**
     * 批量查询
     * @param params 查询参数
     * @return 集合
     */
    @PostMapping("/list")
    @Operation(summary = "批量查询", description = "批量查询")
    @RequestLog(value = "批量查询", response = false)
    public R<List<OrgVo>> list(@RequestBody @Validated OrgQuery params) {
        Org entity = BeanUtil.toBean(params, Org.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<OrgVo> listVo = orgService.listAs(wrapper, OrgVo.class);
        return R.success(listVo);
    }
}
