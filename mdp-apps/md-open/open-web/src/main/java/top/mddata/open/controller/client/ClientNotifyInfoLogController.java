package top.mddata.open.controller.client;

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
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.open.controller.support.PageWrapperHelper;
import top.mddata.open.entity.admin.ApiCallLog;
import top.mddata.open.entity.admin.NotifyInfo;
import top.mddata.open.entity.admin.NotifyInfoLog;
import top.mddata.open.query.admin.NotifyInfoLogQuery;
import top.mddata.open.service.api.ApiCallLogService;
import top.mddata.open.service.notify.callback.NotifyInfoLogService;
import top.mddata.open.service.notify.callback.NotifyInfoService;
import top.mddata.open.vo.admin.NotifyInfoLogVo;

import java.util.List;

/**
 * 我的接口回调日志（开发者自助查询，只读）。
 *
 * @author henhen6
 */
@RestController
@Validated
@Tag(name = "我的接口回调日志")
@RequestMapping("/client/notifyInfoLog")
@RequiredArgsConstructor
public class ClientNotifyInfoLogController {

    private final NotifyInfoLogService notifyInfoLogService;
    private final NotifyInfoService notifyInfoService;
    private final ApiCallLogService apiCallLogService;
    private final ClientAppHelper clientAppHelper;

    /**
     * 分页查询当前开发者的回调日志。支持两种定位：callLogId（经 mdo_notify_info 两级过滤）
     * 或 notifyInfoId（精确过滤），均先做归属校验；都未传时兜底限制在本人应用内。
     * 注：不用 @Validated，NotifyInfoLogQuery.notifyInfoId 的 @NotNull 与 callLogId 场景冲突
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询当前开发者的接口回调日志")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<NotifyInfoLogVo>> page(@RequestBody PageParams<NotifyInfoLogQuery> params) {
        Page<NotifyInfoLogVo> page = Page.of(params.getCurrent(), params.getSize());
        QueryWrapper wrapper = PageWrapperHelper.build(params, NotifyInfoLog.class);
        Long callLogId = params.getModel() == null ? null : params.getModel().getCallLogId();
        Long notifyInfoId = params.getModel() == null ? null : params.getModel().getNotifyInfoId();
        if (callLogId != null) {
            // 越权直接失败，不做静默降级
            ApiCallLog callLog = apiCallLogService.getById(callLogId);
            ArgumentAssert.notNull(callLog, "调用记录不存在");
            clientAppHelper.checkOwner(callLog.getAppId());
            wrapper.and(NotifyInfoLog::getNotifyInfoId).in(QueryWrapper.create()
                    .select(NotifyInfo::getId)
                    .from(NotifyInfo.class)
                    .where(NotifyInfo::getCallLogId).eq(callLogId));
        } else if (notifyInfoId != null) {
            NotifyInfo notifyInfo = notifyInfoService.getById(notifyInfoId);
            ArgumentAssert.notNull(notifyInfo, "回调任务不存在");
            clientAppHelper.checkOwner(notifyInfo.getAppId());
        } else {
            List<Long> appIds = clientAppHelper.myAppIds();
            if (appIds.isEmpty()) {
                return R.success(page);
            }
            wrapper.and(NotifyInfoLog::getNotifyInfoId).in(QueryWrapper.create()
                    .select(NotifyInfo::getId)
                    .from(NotifyInfo.class)
                    .where(NotifyInfo::getAppId).in(appIds));
        }
        notifyInfoLogService.pageAs(page, wrapper, NotifyInfoLogVo.class);
        return R.success(page);
    }
}
