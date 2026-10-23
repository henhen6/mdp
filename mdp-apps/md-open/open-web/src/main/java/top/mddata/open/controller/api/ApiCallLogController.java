package top.mddata.open.controller.api;

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
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.open.controller.support.PageWrapperHelper;
import top.mddata.open.entity.admin.ApiCallLog;
import top.mddata.open.query.admin.ApiCallLogQuery;
import top.mddata.open.service.api.ApiCallLogService;
import top.mddata.open.vo.admin.ApiCallLogVo;

import java.util.List;

/**
 * 调用日志 控制层。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@RestController
@Validated
@Tag(name = "调用日志")
@RequestMapping("/admin/apiCallLog")
@RequiredArgsConstructor
public class ApiCallLogController extends SuperController<ApiCallLogService, ApiCallLog> {

    /**
     * 根据主键删除调用日志。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除调用日志")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }


    /**
     * 分页查询调用日志。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询调用日志")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<ApiCallLogVo>> page(@RequestBody @Validated PageParams<ApiCallLogQuery> params) {
        Page<ApiCallLogVo> page = Page.of(params.getCurrent(), params.getSize());
        superService.pageAs(page, PageWrapperHelper.build(params, ApiCallLog.class), ApiCallLogVo.class);
        return R.success(page);
    }
}
