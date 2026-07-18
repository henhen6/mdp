package top.mddata.workbench.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.workbench.service.dashboard.DashboardWorkbenchService;
import top.mddata.workbench.vo.dashboard.OverviewWorkbenchVo;

/**
 * 系统概览统计 控制层 (workbench部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
@RestController
@Tag(name = "大屏统计-系统概览(workbench)")
@RequestMapping("/dashboard/overview")
@RequiredArgsConstructor
public class DashboardWorkbenchController {

    private final DashboardWorkbenchService dashboardWorkbenchService;

    /**
     * 获取系统概览统计(workbench部分)
     *
     * @return 概览统计
     */
    @GetMapping("/workbench")
    @Operation(summary = "系统概览统计(workbench部分)", description = "获取今日登录次数等")
    @RequestLog(value = "查询系统概览统计(workbench)", logType = RequestLog.LogType.QUERY, response = false)
    public R<OverviewWorkbenchVo> getOverviewWorkbench() {
        return R.success(dashboardWorkbenchService.getOverviewWorkbench());
    }
}