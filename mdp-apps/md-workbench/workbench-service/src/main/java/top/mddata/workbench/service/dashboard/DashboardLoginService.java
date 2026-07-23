package top.mddata.workbench.service.dashboard;

import top.mddata.workbench.vo.dashboard.DailyLoginVo;
import top.mddata.workbench.vo.dashboard.DashboardDistributionVo;
import top.mddata.workbench.vo.dashboard.DashboardRankVo;
import top.mddata.workbench.vo.dashboard.HourlyDistributionVo;
import top.mddata.workbench.vo.dashboard.OverviewLoginVo;
import top.mddata.workbench.vo.dashboard.RegionDistributionVo;

import java.time.LocalDate;
import java.util.List;

/**
 * 登录与安全统计 服务层
 *
 * @author henhen6
 * @since 2026-07-10
 */
public interface DashboardLoginService {

    /**
     * 获取登录概览（今日登录次数与失败次数）
     */
    OverviewLoginVo getOverviewLogin();

    /**
     * 获取登录地域分布
     */
    List<RegionDistributionVo> getRegionDistribution();

    /**
     * 登录省份排行
     */
    List<DashboardRankVo> getProvinceRank(int limit);

    /**
     * 登录IP排行
     */
    List<DashboardRankVo> getIpRank(int limit);

    /**
     * 姓名登录排行
     */
    List<DashboardRankVo> getNameRank(int limit);

    /**
     * 浏览器分布
     */
    List<DashboardDistributionVo> getBrowserDistribution();

    /**
     * 操作系统分布
     */
    List<DashboardDistributionVo> getOsDistribution();

    /**
     * 登录方式分布
     */
    List<DashboardDistributionVo> getAuthTypeDistribution();

    /**
     * 登录渠道分布
     */
    List<DashboardDistributionVo> getChannelDistribution();

    /**
     * 事件类型分布
     */
    List<DashboardDistributionVo> getEventTypeDistribution();

    /**
     * 每日登录统计（返回每天的登录次数和登录人次）
     *
     * @param days 统计天数（7 或 30）
     */
    List<DailyLoginVo> getDailyStatistics(int days);

    /**
     * 活跃用户排行（最近7天）
     */
    List<DashboardRankVo> getActiveUserRank(int limit);

    /**
     * 登录时段分布（按小时统计指定日期）
     *
     * @param date 日期；为空则统计今天
     */
    List<HourlyDistributionVo> getHourlyDistribution(LocalDate date);
}
