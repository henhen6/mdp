package top.mddata.console.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.console.service.dashboard.DashboardMonitorService;
import top.mddata.console.vo.dashboard.ConsumingTimeVo;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.InterfaceRankVo;
import top.mddata.console.vo.dashboard.OverviewMonitorVo;
import top.mddata.console.vo.dashboard.RegionDistributionVo;
import top.mddata.console.vo.dashboard.SuccessRateVo;

import java.util.List;

/**
 * 接口监控统计 控制层
 *
 * @author henhen6
 * @since 2026-07-10
 */
@RestController
@Tag(name = "大屏统计-接口监控(console)")
@RequestMapping("/dashboard/monitor")
@RequiredArgsConstructor
public class DashboardMonitorController {

    private final DashboardMonitorService dashboardMonitorService;

    @GetMapping("/overview")
    @Operation(summary = "接口监控概览", description = "接口总数、今日调用/成功/失败次数、异常请求数")
    @RequestLog(value = "查询接口监控概览", response = false)
    public R<OverviewMonitorVo> getOverview() {
        return R.success(dashboardMonitorService.getOverview());
    }

    @GetMapping("/successRate")
    @Operation(summary = "接口成功率", description = "今日成功/失败/总次数与成功率")
    @RequestLog(value = "查询接口成功率", response = false)
    public R<SuccessRateVo> getSuccessRate() {
        return R.success(dashboardMonitorService.getSuccessRate());
    }

    @GetMapping("/callRank")
    @Operation(summary = "接口调用排行", description = "按调用总次数排序 TOP N")
    @RequestLog(value = "查询接口调用排行", response = false)
    public R<List<InterfaceRankVo>> getCallRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardMonitorService.getCallRank(limit));
    }

    @GetMapping("/failRank")
    @Operation(summary = "接口失败排行", description = "按失败次数排序 TOP N")
    @RequestLog(value = "查询接口失败排行", response = false)
    public R<List<InterfaceRankVo>> getFailRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardMonitorService.getFailRank(limit));
    }

    @GetMapping("/logTypeDistribution")
    @Operation(summary = "请求日志类型分布", description = "按日志类型（查询/新增/修改/删除/其他）统计请求次数")
    @RequestLog(value = "查询请求日志类型分布", response = false)
    public R<List<DistributionVo>> getLogTypeDistribution() {
        return R.success(dashboardMonitorService.getLogTypeDistribution());
    }

    @GetMapping("/regionDistribution")
    @Operation(summary = "请求地域分布", description = "按省份统计请求次数，用于地图展示")
    @RequestLog(value = "查询请求地域分布", response = false)
    public R<List<RegionDistributionVo>> getRegionDistribution() {
        return R.success(dashboardMonitorService.getRegionDistribution());
    }

    @GetMapping("/consumingTimeDistribution")
    @Operation(summary = "请求耗时分布", description = "<100ms / 100-500ms / 500ms-1s / 1s-3s / >=3s")
    @RequestLog(value = "查询请求耗时分布", response = false)
    public R<List<ConsumingTimeVo>> getConsumingTimeDistribution() {
        return R.success(dashboardMonitorService.getConsumingTimeDistribution());
    }
}