package top.mddata.open.controller.notify.callback;

import com.mybatisflex.core.paginate.Page;
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
import top.mddata.base.interfaces.echo.EchoService;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.open.controller.support.PageWrapperHelper;
import top.mddata.open.entity.admin.NotifyInfoLog;
import top.mddata.open.query.admin.NotifyInfoLogQuery;
import top.mddata.open.service.notify.callback.NotifyInfoLogService;
import top.mddata.open.vo.admin.NotifyInfoLogVo;

import java.util.List;

/**
 * 回调任务日志 控制层。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@RestController
@Validated
@Tag(name = "回调任务日志")
@RequestMapping("/admin/notifyInfoLog")
@RequiredArgsConstructor
public class NotifyInfoLogController extends SuperController<NotifyInfoLogService, NotifyInfoLog> {

    private final EchoService echoService;

    /**
     * 根据主键删除回调任务日志。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除回调任务日志")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 分页查询回调任务日志。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询回调任务日志")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<NotifyInfoLogVo>> page(@RequestBody @Validated PageParams<NotifyInfoLogQuery> params) {
        Page<NotifyInfoLogVo> page = Page.of(params.getCurrent(), params.getSize());
        superService.pageAs(page, PageWrapperHelper.build(params, NotifyInfoLog.class), NotifyInfoLogVo.class);
        echoService.action(page);
        return R.success(page);
    }
}
