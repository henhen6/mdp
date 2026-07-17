package top.mddata.open.service.dashboard;

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
import java.util.List;

/**
 * 开放平台统计 服务层
 *
 * @author henhen6
 * @since 2026-07-10
 */
public interface DashboardOpenService {

    /**
     * 开放平台概览
     */
    OverviewOpenVo getOverviewOpen();

    /**
     * API 调用趋势
     *
     * @param startDate 起始日期（包含）
     * @param endDate   截止日期（包含）
     */
    List<CallTrendVo> getCallTrend(String startDate, String endDate);

    /**
     * 应用调用排行
     *
     * @param limit TOP N
     */
    List<AppRankVo> getAppRank(int limit);

    /**
     * API 调用排行
     *
     * @param limit TOP N
     */
    List<ApiRankVo> getApiRank(int limit);

    /**
     * OAuth 授权分布
     */
    List<OauthDistributionVo> getOauthDistribution();

    /**
     * 事件类型触发统计
     */
    List<EventTriggerStatisticsVo> getEventTriggerStatistics();

    /**
     * 事件触发趋势
     *
     * @param startDate 起始日期（包含）
     * @param endDate   截止日期（包含）
     */
    List<EventTriggerTrendVo> getEventTriggerTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 事件触发排行
     *
     * @param limit TOP N
     */
    List<EventTriggerStatisticsVo> getEventTriggerRank(int limit);

    /**
     * 事件应用推送统计
     */
    List<EventPushStatisticsVo> getEventPushStatistics();

    /**
     * 事件推送趋势
     *
     * @param startDate 起始日期（包含）
     * @param endDate   截止日期（包含）
     */
    List<EventPushTrendVo> getEventPushTrend(LocalDate startDate, LocalDate endDate);
}