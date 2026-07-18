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
import top.mddata.console.service.dashboard.DashboardRequestLogService;
import top.mddata.console.vo.dashboard.ConsumingTimeVo;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.IpRankVo;
import top.mddata.console.vo.dashboard.OverviewRequestVo;
import top.mddata.console.vo.dashboard.RegionDistributionVo;
import top.mddata.console.vo.dashboard.RequestInterfaceRankVo;

import java.util.List;

/**
 * 请求日志统计 控制层
 *
 * @author henhen6
 * @since 2026-07-17
 */
@RestController
@Tag(name = "大屏统计-请求日志(console)")
@RequestMapping("/dashboard/requestLog")
@RequiredArgsConstructor
public class DashboardRequestLogController {

    private final DashboardRequestLogService dashboardRequestLogService;

    @GetMapping("/logTypeDistribution")
    @Operation(summary = "请求日志类型分布", description = "按日志类型（查询/新增/修改/删除/其他）统计请求次数")
    @RequestLog(value = "查询请求日志类型分布", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<DistributionVo>> getLogTypeDistribution() {
        return R.success(dashboardRequestLogService.getLogTypeDistribution());
    }

    @GetMapping("/regionDistribution")
    @Operation(summary = "请求地域分布", description = "按省份统计请求次数，用于地图展示")
    @RequestLog(value = "查询请求地域分布", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<RegionDistributionVo>> getRegionDistribution() {
        return R.success(dashboardRequestLogService.getRegionDistribution());
    }

    @GetMapping("/consumingTimeDistribution")
    @Operation(summary = "请求耗时分布", description = "<100ms / 100-500ms / 500ms-1s / 1s-3s / >=3s")
    @RequestLog(value = "查询请求耗时分布", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<ConsumingTimeVo>> getConsumingTimeDistribution() {
        return R.success(dashboardRequestLogService.getConsumingTimeDistribution());
    }

    @GetMapping("/overview")
    @Operation(summary = "请求日志概览", description = "总请求量、异常请求数量、成功请求数量")
    @RequestLog(value = "查询请求日志概览", logType = RequestLog.LogType.QUERY, response = false)
    public R<OverviewRequestVo> getOverview() {
        return R.success(dashboardRequestLogService.getOverview());
    }

    @GetMapping("/ipRank")
    @Operation(summary = "IP地址请求排行", description = "按请求次数统计IP地址排行")
    @RequestLog(value = "查询IP地址请求排行", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<IpRankVo>> getIpRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardRequestLogService.getIpRank(limit));
    }

    @GetMapping("/interfaceRank")
    @Operation(summary = "请求接口排行", description = "按请求次数统计接口排行，前端显示httpUri，hover显示完整信息")
    @RequestLog(value = "查询请求接口排行", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<RequestInterfaceRankVo>> getInterfaceRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardRequestLogService.getInterfaceRank(limit));
    }
}
