package top.mddata.open.controller.client;

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
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.open.controller.support.PageWrapperHelper;
import top.mddata.open.entity.admin.ApiCallLog;
import top.mddata.open.query.admin.ApiCallLogQuery;
import top.mddata.open.service.api.ApiCallLogService;
import top.mddata.open.vo.admin.ApiCallLogVo;

import java.util.List;

/**
 * 我的接口调用日志（开发者自助查询，只读）。
 *
 * @author henhen6
 */
@RestController
@Validated
@Tag(name = "我的接口调用日志")
@RequestMapping("/client/apiCallLog")
@RequiredArgsConstructor
public class ClientApiCallLogController {

    private final ApiCallLogService apiCallLogService;
    private final ClientAppHelper clientAppHelper;

    /**
     * 分页查询当前开发者的接口调用日志。传了 appId 时先校验归属再精确过滤；
     * 未传时兜底限制在本人全部应用内。
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询当前开发者的接口调用日志")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<ApiCallLogVo>> page(@RequestBody @Validated PageParams<ApiCallLogQuery> params) {
        Page<ApiCallLogVo> page = Page.of(params.getCurrent(), params.getSize());
        QueryWrapper wrapper = PageWrapperHelper.build(params, ApiCallLog.class);
        Long appId = params.getModel() == null ? null : params.getModel().getAppId();
        if (appId != null) {
            // 按应用查询：越权直接失败，不做静默降级
            clientAppHelper.checkOwner(appId);
        } else {
            List<Long> appIds = clientAppHelper.myAppIds();
            if (appIds.isEmpty()) {
                return R.success(page);
            }
            wrapper.and(ApiCallLog::getAppId).in(appIds);
        }
        apiCallLogService.pageAs(page, wrapper, ApiCallLogVo.class);
        return R.success(page);
    }

    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取接口调用日志（仅本人应用）")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<ApiCallLogVo> get(@RequestParam Long id) {
        ApiCallLog entity = apiCallLogService.getById(id);
        ArgumentAssert.notNull(entity, "记录不存在");
        clientAppHelper.checkOwner(entity.getAppId());
        return R.success(BeanUtil.toBean(entity, ApiCallLogVo.class));
    }
}
