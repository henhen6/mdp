package top.mddata.console.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewUserVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendVo;

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
    void getUserTrend7Days() {
        List<TrendVo> trend = dashboardUserService.getUserTrend(7);
        assertNotNull(trend);
        // 包含今天的 7 个日期点（含今天）
        assertEquals(7, trend.size());
    }

    @Test
    void getUserTrend30Days() {
        List<TrendVo> trend = dashboardUserService.getUserTrend(30);
        assertNotNull(trend);
        assertEquals(30, trend.size());
    }

    @Test
    void getUserTrendIllegalDaysDefaultsTo7() {
        // 非法值（不是 7 也不是 30）应默认为 7
        List<TrendVo> trend = dashboardUserService.getUserTrend(15);
        assertNotNull(trend);
        assertEquals(7, trend.size());
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
