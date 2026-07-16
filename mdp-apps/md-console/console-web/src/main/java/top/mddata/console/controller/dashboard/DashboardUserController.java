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
import top.mddata.console.service.dashboard.DashboardUserService;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewUserVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendVo;

import java.util.List;

/**
 * 用户与组织统计 控制层
 *
 * @author henhen6
 * @since 2026-07-10
 */
@RestController
@Tag(name = "大屏统计-用户与组织(console)")
@RequestMapping("/dashboard/user")
@RequiredArgsConstructor
public class DashboardUserController {

    private final DashboardUserService dashboardUserService;

    /**
     * 用户与组织概览统计
     */
    @GetMapping("/overview")
    @Operation(summary = "用户与组织概览统计", description = "用户总数、单位数量、部门数量、角色数量")
    @RequestLog(value = "查询用户与组织概览", response = false)
    public R<OverviewUserVo> getOverviewUser() {
        return R.success(dashboardUserService.getOverviewUser());
    }

    /**
     * 用户增长趋势
     */
    @GetMapping("/trend")
    @Operation(summary = "用户增长趋势", description = "按天统计新增用户数，仅支持 7 或 30 天")
    @RequestLog(value = "查询用户增长趋势", response = false)
    public R<List<TrendVo>> getUserTrend(@RequestParam(defaultValue = "7") int days) {
        return R.success(dashboardUserService.getUserTrend(days));
    }

    /**
     * 部门用户排行
     */
    @GetMapping("/orgRank")
    @Operation(summary = "部门用户排行", description = "各部门下的用户数量排行")
    @RequestLog(value = "查询部门用户排行", response = false)
    public R<List<RankVo>> getOrgRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardUserService.getOrgRank(limit));
    }

    /**
     * 角色用户排行
     */
    @GetMapping("/roleRank")
    @Operation(summary = "角色用户排行", description = "各角色下的用户数量排行")
    @RequestLog(value = "查询角色用户排行", response = false)
    public R<List<RankVo>> getRoleRank(@RequestParam(defaultValue = "10") int limit) {
        return R.success(dashboardUserService.getRoleRank(limit));
    }

    /**
     * 用户状态分布
     */
    @GetMapping("/statusDistribution")
    @Operation(summary = "用户状态分布", description = "正常/禁用用户占比")
    @RequestLog(value = "查询用户状态分布", response = false)
    public R<List<DistributionVo>> getStatusDistribution() {
        return R.success(dashboardUserService.getStatusDistribution());
    }

    /**
     * 用户类型分布
     */
    @GetMapping("/typeDistribution")
    @Operation(summary = "用户类型分布", description = "普通用户/管理员/开发者/运维占比")
    @RequestLog(value = "查询用户类型分布", response = false)
    public R<List<DistributionVo>> getTypeDistribution() {
        return R.success(dashboardUserService.getTypeDistribution());
    }
}
