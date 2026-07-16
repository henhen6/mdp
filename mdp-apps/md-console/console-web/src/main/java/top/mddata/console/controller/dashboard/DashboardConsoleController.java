package top.mddata.console.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.console.service.dashboard.DashboardConsoleService;
import top.mddata.console.vo.dashboard.OverviewConsoleVo;

/**
 * 系统概览统计 控制层
 *
 * @author henhen6
 * @since 2026-07-10
 */
@RestController
@Tag(name = "大屏统计-系统概览(console)")
@RequestMapping("/dashboard/overview")
@RequiredArgsConstructor
public class DashboardConsoleController {

    private final DashboardConsoleService dashboardConsoleService;

    /**
     * 获取系统概览统计(console部分)
     *
     * @return 概览统计
     */
    @GetMapping("/console")
    @Operation(summary = "系统概览统计(console部分)", description = "获取用户数、组织数、通知数、文件统计等")
    @RequestLog(value = "查询系统概览统计(console)", response = false)
    public R<OverviewConsoleVo> getOverviewConsole() {
        return R.success(dashboardConsoleService.getOverviewConsole());
    }
}
