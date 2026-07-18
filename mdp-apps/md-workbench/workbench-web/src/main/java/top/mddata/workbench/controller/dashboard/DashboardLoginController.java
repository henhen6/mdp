package top.mddata.workbench.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.workbench.service.dashboard.DashboardLoginService;
import top.mddata.workbench.vo.dashboard.DailyLoginVo;
import top.mddata.workbench.vo.dashboard.DashboardDistributionVo;
import top.mddata.workbench.vo.dashboard.DashboardRankVo;
import top.mddata.workbench.vo.dashboard.HourlyDistributionVo;
import top.mddata.workbench.vo.dashboard.OverviewLoginVo;
import top.mddata.workbench.vo.dashboard.RegionDistributionVo;

import java.util.List;

/**
 * 登录与安全统计 控制层
 *
 * @author henhen6
 * @since 2026-07-10
 */
@RestController
@Tag(name = "大屏统计-登录与安全(workbench)")
@RequestMapping("/dashboard/login")
@RequiredArgsConstructor
public class DashboardLoginController {

    private final DashboardLoginService dashboardLoginService;

    @GetMapping("/overview")
    @Operation(summary = "登录概览", description = "今日登录成功次数与失败次数")
    @RequestLog(value = "查询登录概览", logType = RequestLog.LogType.QUERY, response = false)
    public R<OverviewLoginVo> getOverviewLogin() {
        return R.success(dashboardLoginService.getOverviewLogin());
    }

    @GetMapping("/regionDistribution")
    @Operation(summary = "登录地域分布", description = "按省份统计登录次数（用于地图）")
    @RequestLog(value = "查询登录地域分布", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<RegionDistributionVo>> getRegionDistribution() {
        return R.success(dashboardLoginService.getRegionDistribution());
    }

    @GetMapping("/provinceRank")
    @Operation(summary = "登录省份排行")
    @RequestLog(value = "查询登录省份排行", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<DashboardRankVo>> getProvinceRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardLoginService.getProvinceRank(limit));
    }

    @GetMapping("/ipRank")
    @Operation(summary = "登录IP排行")
    @RequestLog(value = "查询登录IP排行", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<DashboardRankVo>> getIpRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardLoginService.getIpRank(limit));
    }

    @GetMapping("/nameRank")
    @Operation(summary = "姓名登录排行")
    @RequestLog(value = "查询姓名登录排行", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<DashboardRankVo>> getNameRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardLoginService.getNameRank(limit));
    }

    @GetMapping("/browserDistribution")
    @Operation(summary = "浏览器分布")
    @RequestLog(value = "查询浏览器分布", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<DashboardDistributionVo>> getBrowserDistribution() {
        return R.success(dashboardLoginService.getBrowserDistribution());
    }

    @GetMapping("/osDistribution")
    @Operation(summary = "操作系统分布")
    @RequestLog(value = "查询操作系统分布", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<DashboardDistributionVo>> getOsDistribution() {
        return R.success(dashboardLoginService.getOsDistribution());
    }

    @GetMapping("/authTypeDistribution")
    @Operation(summary = "登录方式分布")
    @RequestLog(value = "查询登录方式分布", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<DashboardDistributionVo>> getAuthTypeDistribution() {
        return R.success(dashboardLoginService.getAuthTypeDistribution());
    }

    @GetMapping("/channelDistribution")
    @Operation(summary = "登录渠道分布")
    @RequestLog(value = "查询登录渠道分布", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<DashboardDistributionVo>> getChannelDistribution() {
        return R.success(dashboardLoginService.getChannelDistribution());
    }

    @GetMapping("/eventTypeDistribution")
    @Operation(summary = "事件类型分布")
    @RequestLog(value = "查询事件类型分布", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<DashboardDistributionVo>> getEventTypeDistribution() {
        return R.success(dashboardLoginService.getEventTypeDistribution());
    }

    @GetMapping("/dailyStatistics")
    @Operation(summary = "每日登录统计", description = "返回每天的登录次数与登录人次，仅支持 7 或 30 天")
    @RequestLog(value = "查询每日登录统计", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<DailyLoginVo>> getDailyStatistics(@RequestParam(defaultValue = "7") int days) {
        return R.success(dashboardLoginService.getDailyStatistics(days));
    }

    @GetMapping("/activeUserRank")
    @Operation(summary = "活跃用户排行", description = "最近7天登录次数排行")
    @RequestLog(value = "查询活跃用户排行", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<DashboardRankVo>> getActiveUserRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardLoginService.getActiveUserRank(limit));
    }

    @GetMapping("/hourlyDistribution")
    @Operation(summary = "登录时段分布", description = "按小时统计登录次数；date 为空则统计今天")
    @RequestLog(value = "查询登录时段分布", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<HourlyDistributionVo>> getHourlyDistribution(
            @RequestParam(required = false) String date) {
        return R.success(dashboardLoginService.getHourlyDistribution(date));
    }
}
