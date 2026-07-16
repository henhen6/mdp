package top.mddata.workbench.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.workbench.vo.dashboard.DailyLoginVo;
import top.mddata.workbench.vo.dashboard.DashboardDistributionVo;
import top.mddata.workbench.vo.dashboard.DashboardRankVo;
import top.mddata.workbench.vo.dashboard.HourlyDistributionVo;
import top.mddata.workbench.vo.dashboard.OverviewLoginVo;
import top.mddata.workbench.vo.dashboard.RegionDistributionVo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DashboardLoginService 测试
 *
 * @author henhen6
 * @since 2026-07-10
 */
@SpringBootTest
class DashboardLoginServiceTest {

    @Autowired
    private DashboardLoginService dashboardLoginService;

    @Test
    void getOverviewLogin() {
        OverviewLoginVo vo = dashboardLoginService.getOverviewLogin();
        assertNotNull(vo);
        assertNotNull(vo.getTodayLoginCount());
        assertNotNull(vo.getTodayFailCount());
    }

    @Test
    void getRegionDistribution() {
        List<RegionDistributionVo> result = dashboardLoginService.getRegionDistribution();
        assertNotNull(result);
    }

    @Test
    void getProvinceRank() {
        List<DashboardRankVo> result = dashboardLoginService.getProvinceRank(5);
        assertNotNull(result);
        assertTrue(result.size() <= 5);
    }

    @Test
    void getIpRank() {
        List<DashboardRankVo> result = dashboardLoginService.getIpRank(10);
        assertNotNull(result);
        assertTrue(result.size() <= 10);
    }

    @Test
    void getNameRank() {
        List<DashboardRankVo> result = dashboardLoginService.getNameRank(10);
        assertNotNull(result);
        assertTrue(result.size() <= 10);
    }

    @Test
    void getBrowserDistribution() {
        List<DashboardDistributionVo> result = dashboardLoginService.getBrowserDistribution();
        assertNotNull(result);
    }

    @Test
    void getOsDistribution() {
        List<DashboardDistributionVo> result = dashboardLoginService.getOsDistribution();
        assertNotNull(result);
    }

    @Test
    void getAuthTypeDistribution() {
        List<DashboardDistributionVo> result = dashboardLoginService.getAuthTypeDistribution();
        assertNotNull(result);
    }

    @Test
    void getChannelDistribution() {
        List<DashboardDistributionVo> result = dashboardLoginService.getChannelDistribution();
        assertNotNull(result);
    }

    @Test
    void getEventTypeDistribution() {
        List<DashboardDistributionVo> result = dashboardLoginService.getEventTypeDistribution();
        assertNotNull(result);
    }

    @Test
    void getDailyStatistics7Days() {
        List<DailyLoginVo> result = dashboardLoginService.getDailyStatistics(7);
        assertNotNull(result);
        assertEquals(7, result.size());
    }

    @Test
    void getDailyStatistics30Days() {
        List<DailyLoginVo> result = dashboardLoginService.getDailyStatistics(30);
        assertNotNull(result);
        assertEquals(30, result.size());
    }

    @Test
    void getActiveUserRank() {
        List<DashboardRankVo> result = dashboardLoginService.getActiveUserRank(10);
        assertNotNull(result);
        assertTrue(result.size() <= 10);
    }

    @Test
    void getHourlyDistribution() {
        List<HourlyDistributionVo> result = dashboardLoginService.getHourlyDistribution(null);
        assertNotNull(result);
        assertEquals(24, result.size());
    }
}
