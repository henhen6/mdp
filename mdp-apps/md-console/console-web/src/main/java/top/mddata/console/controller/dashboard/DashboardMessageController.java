package top.mddata.console.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.console.service.dashboard.DashboardMessageService;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewMessageVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendLineVo;

import java.util.List;

/**
 * 消息通知统计 控制层
 *
 * @author henhen6
 * @since 2026-07-10
 */
@RestController
@Tag(name = "大屏统计-消息通知(console)")
@RequestMapping("/dashboard/message")
@RequiredArgsConstructor
public class DashboardMessageController {

    private final DashboardMessageService dashboardMessageService;

    @GetMapping("/overview")
    @Operation(summary = "消息通知概览", description = "任务总数、今日发送数、待执行数、草稿数、成功数、失败数")
    @RequestLog(value = "查询消息通知概览", response = false)
    public R<OverviewMessageVo> getOverviewMessage() {
        return R.success(dashboardMessageService.getOverviewMessage());
    }

    @GetMapping("/typeDistribution")
    @Operation(summary = "消息类型分布", description = "站内信/短信/邮件")
    @RequestLog(value = "查询消息类型分布", response = false)
    public R<List<DistributionVo>> getTypeDistribution() {
        return R.success(dashboardMessageService.getTypeDistribution());
    }

    @GetMapping("/categoryDistribution")
    @Operation(summary = "消息分类分布", description = "待办/公告/预警（本地查询 mdc_msg_task，条件 type=1 AND status=2）")
    @RequestLog(value = "查询消息分类分布", response = false)
    public R<List<DistributionVo>> getCategoryDistribution() {
        return R.success(dashboardMessageService.getCategoryDistribution());
    }

    @GetMapping("/trend")
    @Operation(summary = "消息发送趋势", description = "按天统计成功发送数，支持日期区间筛选，返回各类型数量和总量")
    @RequestLog(value = "查询消息发送趋势", response = false)
    public R<List<TrendLineVo>> getTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer type) {
        return R.success(dashboardMessageService.getTrend(startDate, endDate, type));
    }

    @GetMapping("/templateRank")
    @Operation(summary = "消息模板使用排行", description = "只统计 state=1 的模板")
    @RequestLog(value = "查询消息模板使用排行", response = false)
    public R<List<RankVo>> getTemplateRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardMessageService.getTemplateRank(limit));
    }
}
