package top.mddata.console.service.dashboard;

import top.mddata.console.vo.dashboard.ConsumingTimeVo;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.RegionDistributionVo;

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
     * 异常请求数量
     */
    Long getAbnormalCount();
}
