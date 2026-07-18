package top.mddata.open.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.open.service.dashboard.DashboardOpenService;
import top.mddata.open.vo.dashboard.ApiRankVo;
import top.mddata.open.vo.dashboard.AppRankVo;
import top.mddata.open.vo.dashboard.CallTrendVo;
import top.mddata.open.vo.dashboard.EventPushStatisticsVo;
import top.mddata.open.vo.dashboard.EventPushTrendVo;
import top.mddata.open.vo.dashboard.EventTriggerStatisticsVo;
import top.mddata.open.vo.dashboard.EventTriggerTrendVo;
import top.mddata.open.vo.dashboard.OauthDistributionVo;
import top.mddata.open.vo.dashboard.OverviewOpenVo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 开放平台统计 控制层
 *
 * @author henhen6
 * @since 2026-07-10
 */
@RestController
@Tag(name = "大屏统计-开放平台(open)-详细统计")
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardOpenDetailController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DashboardOpenService dashboardOpenService;


    @GetMapping("/open/overview")
    @Operation(summary = "开放平台概览", description = "应用总数、自建应用数、第三方应用数、API总数、今日调用量、今日失败数、待审批数")
    @RequestLog(value = "查询开放平台概览", logType = RequestLog.LogType.QUERY, response = false)
    public R<OverviewOpenVo> getOverviewOpen() {
        return R.success(dashboardOpenService.getOverviewOpen());
    }

    @GetMapping("/callTrend")
    @Operation(summary = "API调用趋势", description = "按天统计调用总数与失败数，支持日期区间查询（yyyy-MM-dd），默认最近7天")
    @RequestLog(value = "查询API调用趋势", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<CallTrendVo>> getCallTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return R.success(dashboardOpenService.getCallTrend(startDate, endDate));
    }

    @GetMapping("/appRank")
    @Operation(summary = "应用调用排行", description = "按调用次数统计应用排行 TOP N")
    @RequestLog(value = "查询应用调用排行", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<AppRankVo>> getAppRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardOpenService.getAppRank(limit));
    }

    @GetMapping("/apiRank")
    @Operation(summary = "API调用排行", description = "按调用次数统计接口排行 TOP N")
    @RequestLog(value = "查询API调用排行", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<ApiRankVo>> getApiRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardOpenService.getApiRank(limit));
    }

    @GetMapping("/oauthDistribution")
    @Operation(summary = "OAuth授权分布", description = "按 grant_type 分组统计授权次数")
    @RequestLog(value = "查询OAuth授权分布", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<OauthDistributionVo>> getOauthDistribution() {
        return R.success(dashboardOpenService.getOauthDistribution());
    }

    @GetMapping("/event/trigger/statistics")
    @Operation(summary = "事件触发统计", description = "按事件类型分组统计触发次数")
    @RequestLog(value = "查询事件触发统计", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<EventTriggerStatisticsVo>> getEventTriggerStatistics() {
        return R.success(dashboardOpenService.getEventTriggerStatistics());
    }

    @GetMapping("/event/trigger/trend")
    @Operation(summary = "事件触发趋势", description = "按天统计事件触发次数，支持日期区间查询（yyyy-MM-dd），默认最近7天")
    @RequestLog(value = "查询事件触发趋势", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<EventTriggerTrendVo>> getEventTriggerTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return R.success(dashboardOpenService.getEventTriggerTrend(parseDate(startDate), parseDate(endDate)));
    }

    @GetMapping("/event/trigger/rank")
    @Operation(summary = "事件触发排行榜", description = "按事件类型分组统计触发次数 TOP N")
    @RequestLog(value = "查询事件触发排行", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<EventTriggerStatisticsVo>> getEventTriggerRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardOpenService.getEventTriggerRank(limit));
    }

    @GetMapping("/event/push/statistics")
    @Operation(summary = "事件应用推送统计", description = "按事件类型+应用分组，统计推送次数")
    @RequestLog(value = "查询事件应用推送统计", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<EventPushStatisticsVo>> getEventPushStatistics() {
        return R.success(dashboardOpenService.getEventPushStatistics());
    }

    @GetMapping("/event/push/trend")
    @Operation(summary = "事件推送趋势", description = "按天统计触发次数与推送请求次数，支持日期区间查询（yyyy-MM-dd），默认最近7天")
    @RequestLog(value = "查询事件推送趋势", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<EventPushTrendVo>> getEventPushTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return R.success(dashboardOpenService.getEventPushTrend(parseDate(startDate), parseDate(endDate)));
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}