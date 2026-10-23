package top.mddata.open.controller.app;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import top.mddata.open.dto.admin.AppScopeApplyReviewDto;
import top.mddata.open.entity.admin.App;
import top.mddata.open.entity.admin.AppScopeApply;
import top.mddata.open.entity.admin.ScopeGroup;
import top.mddata.open.query.admin.AppScopeApplyQuery;
import top.mddata.open.service.app.AppScopeApplyService;
import top.mddata.open.service.app.AppService;
import top.mddata.open.service.oauth.ScopeGroupService;
import top.mddata.open.vo.admin.AppScopeApplyVo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 权限组申请审核 控制层（控制台）。
 *
 * @author henhen6
 */
@RestController
@Validated
@Tag(name = "权限组申请审核")
@RequestMapping("/admin/appScopeApply")
@RequiredArgsConstructor
public class AppScopeApplyController extends SuperController<AppScopeApplyService, AppScopeApply> {

    private final ScopeGroupService scopeGroupService;
    private final AppService appService;

    /**
     * 分页查询权限组申请
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询权限组申请")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'",
            logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<AppScopeApplyVo>> page(@RequestBody @Validated PageParams<AppScopeApplyQuery> params) {
        Page<AppScopeApplyVo> page = Page.of(params.getCurrent(), params.getSize());
        AppScopeApply entity = BeanUtil.toBean(params.getModel(), AppScopeApply.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, AppScopeApplyVo.class);
        fillNames(page);
        return R.success(page);
    }

    /**
     * 根据主键查询申请单
     *
     * @param id 主键
     * @return 申请单
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键查询申请单")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<AppScopeApplyVo> getById(@RequestParam Long id) {
        AppScopeApply entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, AppScopeApplyVo.class));
    }

    /**
     * 审核
     *
     * @param dto 审核参数
     * @return 是否成功
     */
    @PostMapping("/review")
    @Operation(summary = "审核", description = "审核权限组申请")
    @RequestLog(value = "审核权限组申请", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Boolean> review(@Validated @RequestBody AppScopeApplyReviewDto dto) {
        return R.success(superService.review(dto));
    }

    /**
     * 回显应用名称与权限组名称（申请单只存 appId/groupId，列表需展示名称）
     */
    private void fillNames(Page<AppScopeApplyVo> page) {
        List<AppScopeApplyVo> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return;
        }
        Set<Long> appIds = records.stream().map(AppScopeApplyVo::getAppId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> groupIds = records.stream().map(AppScopeApplyVo::getGroupId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, App> appMap = appIds.isEmpty() ? Map.of()
                : appService.listByIds(appIds).stream()
                .collect(Collectors.toMap(App::getId, Function.identity()));
        Map<Long, ScopeGroup> groupMap = groupIds.isEmpty() ? Map.of()
                : scopeGroupService.listByIds(groupIds).stream()
                .collect(Collectors.toMap(ScopeGroup::getId, Function.identity()));
        records.forEach(vo -> {
            App app = appMap.get(vo.getAppId());
            ScopeGroup group = groupMap.get(vo.getGroupId());
            vo.setAppName(app == null ? null : app.getName());
            vo.setGroupName(group == null ? null : group.getName());
        });
    }
}
