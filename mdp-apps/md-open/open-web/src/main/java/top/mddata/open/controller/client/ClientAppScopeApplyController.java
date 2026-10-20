package top.mddata.open.controller.client;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.paginate.Page;
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
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.dto.IdDto;
import top.mddata.open.dto.admin.AppScopeApplyDto;
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
 * 权限组申请 控制层（开发者端）。
 *
 * @author henhen6
 */
@RestController
@Validated
@Tag(name = "权限组申请-开发者端")
@RequestMapping("/client/appScopeApply")
@RequiredArgsConstructor
public class ClientAppScopeApplyController {

    private final AppScopeApplyService appScopeApplyService;
    private final ScopeGroupService scopeGroupService;
    private final AppService appService;

    /**
     * 批量申请权限组
     *
     * @param dto 申请参数
     * @return 生成的申请单数量
     */
    @PostMapping("/apply")
    @Operation(summary = "申请权限组", description = "一单一组，批量提交")
    @RequestLog(value = "申请权限组", logType = RequestLog.LogType.ADD)
    public R<Integer> apply(@Validated @RequestBody AppScopeApplyDto dto) {
        return R.success(appScopeApplyService.apply(dto));
    }

    /**
     * 撤销申请单（仅待审批状态）
     *
     * @param dto 申请单ID
     * @return 是否成功
     */
    @PostMapping("/withdraw")
    @Operation(summary = "撤销申请", description = "仅待审批的申请可以撤销")
    @RequestLog(value = "撤销权限组申请", logType = RequestLog.LogType.UPDATE)
    public R<Boolean> withdraw(@Validated @RequestBody IdDto dto) {
        return R.success(appScopeApplyService.withdraw(dto.getId()));
    }

    /**
     * 删除申请单（仅已撤销状态）
     *
     * @param ids 申请单ID列表
     * @return 是否成功
     */
    @PostMapping("/delete")
    @Operation(summary = "删除申请", description = "仅已撤销的申请可以删除")
    @RequestLog(value = "删除权限组申请", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(appScopeApplyService.deleteByIds(ids));
    }

    /**
     * 分页查询我的申请记录（仅本人创建的应用）
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询申请记录", description = "仅本人名下应用")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'",
            logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<AppScopeApplyVo>> page(@RequestBody @Validated PageParams<AppScopeApplyQuery> params) {
        Page<AppScopeApplyVo> page = Page.of(params.getCurrent(), params.getSize());
        AppScopeApply entity = BeanUtil.toBean(params.getModel(), AppScopeApply.class);
        // 归属过滤：只能看本人创建的申请单
        entity.setCreatedBy(ContextUtil.getUserId());
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        appScopeApplyService.pageAs(page, wrapper, AppScopeApplyVo.class);
        fillNames(page);
        return R.success(page);
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
