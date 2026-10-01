package top.mddata.console.service.dashboard;

import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewUserVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendChartVo;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户与组织统计 服务层
 *
 * @author henhen6
 * @since 2026-07-10
 */
public interface DashboardUserService {

    /**
     * 获取用户与组织概览统计
     *
     * @return 用户与组织概览
     */
    OverviewUserVo getOverviewUser();

    /**
     * 获取用户增长趋势
     *
     * @param startDate 开始日期（默认近30天）
     * @param endDate   截止日期（默认今天）
     * @return 用户增长趋势数据（运营=总用户/总公司/开发者/运营 4条曲线，其他性质=新增用户 1条曲线）
     */
    TrendChartVo getUserTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 获取部门用户排行
     *
     * @param limit 排行榜上限
     * @return 部门用户排行
     */
    List<RankVo> getOrgRank(int limit);

    /**
     * 获取角色用户排行
     *
     * @param limit 排行榜上限
     * @return 角色用户排行
     */
    List<RankVo> getRoleRank(int limit);

    /**
     * 获取用户状态分布
     *
     * @param startDate 开始日期（默认全部时间）
     * @param endDate   截止日期（默认全部时间）
     * @return 用户状态分布
     */
    List<DistributionVo> getStatusDistribution(LocalDate startDate, LocalDate endDate);

    /**
     * 获取新增用户性别分布
     *
     * @param startDate 开始日期（默认全部时间）
     * @param endDate   截止日期（默认全部时间）
     * @return 新增用户性别分布
     */
    List<DistributionVo> getGenderDistribution(LocalDate startDate, LocalDate endDate);

    /**
     * 获取用户类型分布（仅运营性质可查，其他性质返回空列表）
     *
     * @param startDate 开始日期（默认全部时间）
     * @param endDate   截止日期（默认全部时间）
     * @return 用户类型分布
     */
    List<DistributionVo> getTypeDistribution(LocalDate startDate, LocalDate endDate);
}
