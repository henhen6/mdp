package top.mddata.console.service.dashboard;

import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewUserVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendVo;

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
     * @param days 统计天数（7 或 30）
     * @return 用户增长趋势数据
     */
    List<TrendVo> getUserTrend(int days);

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
     * @return 用户状态分布
     */
    List<DistributionVo> getStatusDistribution();

    /**
     * 获取用户类型分布
     *
     * @return 用户类型分布
     */
    List<DistributionVo> getTypeDistribution();
}
