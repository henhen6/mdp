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
import top.mddata.open.entity.admin.EventPush;
import top.mddata.open.query.admin.EventPushQuery;
import top.mddata.open.service.notify.event.EventPushService;
import top.mddata.open.vo.admin.EventPushVo;

import java.util.List;

/**
 * 我的事件推送记录（开发者自助查询，只读）。
 *
 * @author henhen6
 */
@RestController
@Validated
@Tag(name = "我的事件推送记录")
@RequestMapping("/client/eventPush")
@RequiredArgsConstructor
public class ClientEventPushController {

    private final EventPushService eventPushService;
    private final ClientAppHelper clientAppHelper;

    /**
     * 分页查询当前开发者的事件推送记录。传了 appId 时先校验归属再精确过滤；
     * 未传时兜底限制在本人全部应用内。
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询当前开发者的事件推送记录")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<EventPushVo>> page(@RequestBody @Validated PageParams<EventPushQuery> params) {
        Page<EventPushVo> page = Page.of(params.getCurrent(), params.getSize());
        QueryWrapper wrapper = PageWrapperHelper.build(params, EventPush.class);
        Long appId = params.getModel() == null ? null : params.getModel().getAppId();
        if (appId != null) {
            // 按应用查询：越权直接失败，不做静默降级
            clientAppHelper.checkOwner(appId);
        } else {
            List<Long> appIds = clientAppHelper.myAppIds();
            if (appIds.isEmpty()) {
                return R.success(page);
            }
            wrapper.and(EventPush::getAppId).in(appIds);
        }
        eventPushService.pageAs(page, wrapper, EventPushVo.class);
        return R.success(page);
    }

    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取事件推送记录（仅本人应用）")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<EventPushVo> get(@RequestParam Long id) {
        EventPush entity = eventPushService.getById(id);
        ArgumentAssert.notNull(entity, "记录不存在");
        clientAppHelper.checkOwner(entity.getAppId());
        return R.success(BeanUtil.toBean(entity, EventPushVo.class));
    }
}
