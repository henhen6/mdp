package top.mddata.open.admin.controller;

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
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.common.enumeration.AuditStatusEnum;
import top.mddata.open.admin.dto.AppApplyReviewDto;
import top.mddata.open.admin.entity.AppApply;
import top.mddata.open.admin.query.AppApplyQuery;
import top.mddata.open.admin.service.AppApplyService;
import top.mddata.open.admin.vo.AppApplyVo;

import java.util.Arrays;
import java.util.List;

/**
 * 应用申请 控制层。
 *
 * @author henhen6
 * @since 2025-11-27 03:31:55
 */
@RestController
@Validated
@Tag(name = "应用申请")
@RequestMapping("/admin/appApply")
@RequiredArgsConstructor
public class AppApplyController extends SuperController<AppApplyService, AppApply> {

    /**
     * 根据主键删除应用申请。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除应用申请")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }


    /**
     * 根据应用申请主键获取详细信息。
     *
     * @param id 应用申请主键
     * @return 应用申请详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取应用申请")
    @RequestLog("'单体查询:' + #id")
    public R<AppApplyVo> get(@RequestParam Long id) {
        AppApply entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, AppApplyVo.class));
    }

    /**
     * 分页查询应用申请。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询应用申请")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<AppApplyVo>> page(@RequestBody @Validated PageParams<AppApplyQuery> params) {
        Page<AppApplyVo> page = Page.of(params.getCurrent(), params.getSize());
        AppApply entity = BeanUtil.toBean(params.getModel(), AppApply.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        wrapper.in(AppApply::getAuditStatus, Arrays.asList(AuditStatusEnum.PENDING.getCode(), AuditStatusEnum.APPROVED.getCode(), AuditStatusEnum.REJECTED.getCode()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, AppApplyVo.class);
        return R.success(page);
    }


    /**
     * 审核。
     *
     * @param dto 审核参数
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/review")
    @Operation(summary = "审核应用", description = "根据主键审核应用")
    @RequestLog(value = "审核应用", request = false)
    public R<Boolean> review(@Validated @RequestBody AppApplyReviewDto dto) {
        return R.success(superService.review(dto));
    }

}
