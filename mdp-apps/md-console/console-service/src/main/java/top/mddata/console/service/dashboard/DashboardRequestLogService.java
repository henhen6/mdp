package top.mddata.console.service.dashboard;

import top.mddata.console.vo.dashboard.ConsumingTimeVo;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.IpRankVo;
import top.mddata.console.vo.dashboard.OverviewRequestVo;
import top.mddata.console.vo.dashboard.RegionDistributionVo;
import top.mddata.console.vo.dashboard.RequestInterfaceRankVo;

import java.util.List;

/**
 * 请求日志统计 服务层
 *
 * <p>数据来源：mdc_request_log</p>
 *
 * @author henhen6
 * @since 2026-07-17
 */
public interface DashboardRequestLogService {

    /**
     * 请求日志类型分布
     */
    List<DistributionVo> getLogTypeDistribution();

    /**
     * 请求地域分布
     */
    List<RegionDistributionVo> getRegionDistribution();

    /**
     * 请求耗时分布
     */
    List<ConsumingTimeVo> getConsumingTimeDistribution();

    /**
     * 请求概览（总请求量、异常请求数量、成功请求数量）
     */
    OverviewRequestVo getOverview();

    /**
     * IP地址请求排行
     */
    List<IpRankVo> getIpRank(int limit);

    /**
     * 请求接口排行（class_path + method_name 唯一标识）
     */
    List<RequestInterfaceRankVo> getInterfaceRank(int limit);
}
