package top.mddata.console.service.dashboard;

import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewMessageVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendVo;

import java.util.List;

/**
 * 消息通知统计 服务层
 *
 * @author henhen6
 * @since 2026-07-10
 */
public interface DashboardMessageService {

    /**
     * 消息通知概览（任务总数、今日发送数、待执行数）
     */
    OverviewMessageVo getOverviewMessage();

    /**
     * 消息类型分布
     */
    List<DistributionVo> getTypeDistribution();

    /**
     * 消息分类分布（通过 NoticeFacade 跨服务统计 mdc_notice 表）
     */
    List<DistributionVo> getCategoryDistribution();

    /**
     * 消息发送趋势（按天统计成功发送数）
     *
     * @param days 统计天数（7 或 30）
     */
    List<TrendVo> getTrend(int days);

    /**
     * 消息模板使用排行
     *
     * @param limit 排行榜上限
     */
    List<RankVo> getTemplateRank(int limit);
}
