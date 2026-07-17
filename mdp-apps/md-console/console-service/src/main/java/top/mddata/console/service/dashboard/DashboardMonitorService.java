package top.mddata.console.service.dashboard;

import top.mddata.console.vo.dashboard.InterfaceRankVo;
import top.mddata.console.vo.dashboard.OverviewMonitorVo;
import top.mddata.console.vo.dashboard.SuccessRateVo;

import java.util.List;

/**
 * 接口监控统计 服务层
 *
 * <p>数据来源：mdc_interface_config / mdc_interface_stat / mdc_interface_log</p>
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
}
