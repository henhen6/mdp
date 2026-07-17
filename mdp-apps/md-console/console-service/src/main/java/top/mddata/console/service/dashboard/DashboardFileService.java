package top.mddata.console.service.dashboard;

import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.FileTrendVo;
import top.mddata.console.vo.dashboard.OverviewFileVo;

import java.util.List;

/**
 * 文件存储统计 服务层
 *
 * @author henhen6
 * @since 2026-07-10
 */
public interface DashboardFileService {

    /**
     * 文件存储概览
     */
    OverviewFileVo getOverviewFile();

    /**
     * 文件类型分布
     */
    List<DistributionVo> getFileTypeDistribution();

    /**
     * 业务类型分布
     */
    List<DistributionVo> getObjectTypeDistribution();

    /**
     * 存储平台分布
     */
    List<DistributionVo> getPlatformDistribution();

    /**
     * 文件大小分布
     */
    List<DistributionVo> getSizeDistribution();

    /**
     * 文件增长趋势
     *
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate   结束日期（yyyy-MM-dd）
     */
    List<FileTrendVo> getTrend(String startDate, String endDate);
}
