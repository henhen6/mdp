package top.mddata.console.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewUserVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendVo;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DashboardUserService 测试
 *
 * @author henhen6
 * @since 2026-07-10
 */
@SpringBootTest
class DashboardUserServiceTest {

    @Autowired
    private DashboardUserService dashboardUserService;

    @Test
    void getOverviewUser() {
        OverviewUserVo vo = dashboardUserService.getOverviewUser();
        assertNotNull(vo);
        assertNotNull(vo.getUserCount());
        assertNotNull(vo.getCompanyCount());
        assertNotNull(vo.getDeptCount());
        assertNotNull(vo.getRoleCount());
        assertTrue(vo.getUserCount() >= 0);
        assertTrue(vo.getCompanyCount() >= 0);
        assertTrue(vo.getDeptCount() >= 0);
        assertTrue(vo.getRoleCount() >= 0);
    }

    @Test
    void getUserTrendWithDateRange() {
        // 指定日期范围
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 7);
        List<TrendVo> trend = dashboardUserService.getUserTrend(start, end);
        assertNotNull(trend);
        assertEquals(7, trend.size());
    }

    @Test
    void getUserTrendNullDefaultsToLast7Days() {
        // null 参数应默认近7天
        List<TrendVo> trend = dashboardUserService.getUserTrend(null, null);
        assertNotNull(trend);
        assertEquals(7, trend.size());
    }

    @Test
    void getUserTrendOnlyStartDate() {
        // 只有开始日期
        LocalDate start = LocalDate.of(2026, 7, 1);
        List<TrendVo> trend = dashboardUserService.getUserTrend(start, null);
        assertNotNull(trend);
    }

    @Test
    void getUserTrendOnlyEndDate() {
        // 只有结束日期
        LocalDate end = LocalDate.of(2026, 7, 7);
        List<TrendVo> trend = dashboardUserService.getUserTrend(null, end);
        assertNotNull(trend);
    }

    @Test
    void getOrgRank() {
        List<RankVo> rank = dashboardUserService.getOrgRank(5);
        assertNotNull(rank);
        assertTrue(rank.size() <= 5);
    }

    @Test
    void getRoleRank() {
        List<RankVo> rank = dashboardUserService.getRoleRank(10);
        assertNotNull(rank);
        assertTrue(rank.size() <= 10);
    }

    @Test
    void getStatusDistribution() {
        List<DistributionVo> distribution = dashboardUserService.getStatusDistribution();
        assertNotNull(distribution);
    }

    @Test
    void getTypeDistribution() {
        List<DistributionVo> distribution = dashboardUserService.getTypeDistribution();
        assertNotNull(distribution);
    }
}
