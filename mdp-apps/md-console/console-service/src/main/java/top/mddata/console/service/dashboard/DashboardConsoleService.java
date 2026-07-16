package top.mddata.console.service.dashboard;

import top.mddata.console.vo.dashboard.OverviewConsoleVo;

/**
 * 系统概览统计 服务层
 *
 * @author henhen6
 * @since 2026-07-10
 */
public interface DashboardConsoleService {

    /**
     * 获取系统概览统计(console部分)
     *
     * @return 概览统计
     */
    OverviewConsoleVo getOverviewConsole();
}
