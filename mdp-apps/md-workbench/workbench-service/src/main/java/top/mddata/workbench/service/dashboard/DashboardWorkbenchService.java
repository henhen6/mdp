package top.mddata.workbench.service.dashboard;

import top.mddata.workbench.vo.dashboard.OverviewWorkbenchVo;

/**
 * 系统概览统计 服务层 (workbench部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
public interface DashboardWorkbenchService {

    /**
     * 获取系统概览统计(workbench部分)
     *
     * @return 概览统计
     */
    OverviewWorkbenchVo getOverviewWorkbench();
}