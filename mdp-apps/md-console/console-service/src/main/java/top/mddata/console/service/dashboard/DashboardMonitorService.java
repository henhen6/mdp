package top.mddata.console.service.dashboard;

import top.mddata.console.vo.dashboard.ConsumingTimeVo;
import top.mddata.console.vo.dashboard.InterfaceRankVo;
import top.mddata.console.vo.dashboard.OverviewMonitorVo;
import top.mddata.console.vo.dashboard.RegionDistributionVo;
import top.mddata.console.vo.dashboard.SuccessRateVo;

import java.util.List;

/**
 * 接口监控统计 服务层
 *
 * @author henhen6
 * @since 2026-07-10
 */
public interface DashboardMonitorService {

    /**
     * 接口监控概览
     */
    OverviewMonitorVo getOverview();

    /**
     * 接口成功率
     */
    SuccessRateVo getSuccessRate();

    /**
     * 接口调用排行
     *
     * @param limit TOP N
     */
    List<InterfaceRankVo> getCallRank(int limit);

    /**
     * 接口失败排行
     *
     * @param limit TOP N
     */
    List<InterfaceRankVo> getFailRank(int limit);

    /**
     * 请求日志类型分布
     */
    List<top.mddata.console.vo.dashboard.DistributionVo> getLogTypeDistribution();

    /**
     * 请求地域分布
     */
    List<RegionDistributionVo> getRegionDistribution();

    /**
     * 请求耗时分布
     */
    List<ConsumingTimeVo> getConsumingTimeDistribution();
}