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
import top.mddata.open.controller.support.PageWrapperHelper;
import top.mddata.open.entity.admin.NotifyInfo;
import top.mddata.open.query.admin.NotifyInfoQuery;
import top.mddata.open.service.notify.callback.NotifyInfoService;
import top.mddata.open.vo.admin.NotifyInfoVo;

import java.util.List;

/**
 * 我的接口回调记录（开发者自助查询，只读）。
 *
 * @author henhen6
 */
@RestController
@Validated
@Tag(name = "我的接口回调记录")
@RequestMapping("/client/notifyInfo")
@RequiredArgsConstructor
public class ClientNotifyInfoController {

    private final NotifyInfoService notifyInfoService;
    private final ClientAppHelper clientAppHelper;

    /**
     * 分页查询当前开发者的回调记录。传了 appId 时先校验归属再精确过滤；
     * 未传时兜底限制在本人全部应用内。
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询当前开发者的接口回调记录")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<NotifyInfoVo>> page(@RequestBody @Validated PageParams<NotifyInfoQuery> params) {
        Page<NotifyInfoVo> page = Page.of(params.getCurrent(), params.getSize());
        QueryWrapper wrapper = PageWrapperHelper.build(params, NotifyInfo.class);
        Long appId = params.getModel() == null ? null : params.getModel().getAppId();
        if (appId != null) {
            // 按应用查询：越权直接失败，不做静默降级
            clientAppHelper.checkOwner(appId);
        } else {
            List<Long> appIds = clientAppHelper.myAppIds();
            if (appIds.isEmpty()) {
                return R.success(page);
            }
            wrapper.and(NotifyInfo::getAppId).in(appIds);
        }
        notifyInfoService.pageAs(page, wrapper, NotifyInfoVo.class);
        return R.success(page);
    }

}
