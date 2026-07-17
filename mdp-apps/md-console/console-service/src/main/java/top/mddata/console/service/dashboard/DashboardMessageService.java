package top.mddata.console.service.dashboard;

import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewMessageVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendLineVo;

import java.util.List;

/**
 * 消息通知统计 服务层
 *
 * @author henhen6
 * @since 2026-07-10
 */
public interface DashboardMessageService {

    /**
     * 消息通知概览（任务总数、今日发送数、待执行数、草稿数、成功数、失败数）
     */
    OverviewMessageVo getOverviewMessage();

    /**
     * 消息类型分布
     */
    List<DistributionVo> getTypeDistribution();

    /**
     * 消息分类分布（本地查询 mdc_msg_task，条件 type=1 AND status=2）
     */
    List<DistributionVo> getCategoryDistribution();

    /**
     * 消息发送趋势（按天统计成功发送数，支持日期区间和消息类型筛选）
     *
     * @param startDate 开始日期（yyyy-MM-dd，默认近7天）
     * @param endDate   结束日期（yyyy-MM-dd，默认今天）
     * @param type      消息类型（1-站内信 2-短信 3-邮件，不传查所有）
     * @return 趋势数据（包含各类型数量和总量）
     */
    List<TrendLineVo> getTrend(String startDate, String endDate, Integer type);

    /**
     * 消息模板使用排行
     *
     * @param limit 排行榜上限
     */
    List<RankVo> getTemplateRank(int limit);
}
