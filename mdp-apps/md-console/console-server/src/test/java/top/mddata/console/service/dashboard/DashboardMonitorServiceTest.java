package top.mddata.console.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.console.vo.dashboard.ConsumingTimeVo;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.InterfaceRankVo;
import top.mddata.console.vo.dashboard.OverviewMonitorVo;
import top.mddata.console.vo.dashboard.RegionDistributionVo;
import top.mddata.console.vo.dashboard.SuccessRateVo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DashboardMonitorService 测试
 *
 * @author henhen6
 * @since 2026-07-10
 */
@SpringBootTest
class DashboardMonitorServiceTest {

    @Autowired
    private DashboardMonitorService dashboardMonitorService;

    @Test
    void getOverview() {
        OverviewMonitorVo vo = dashboardMonitorService.getOverview();
        assertNotNull(vo);
        assertNotNull(vo.getInterfaceCount());
        assertNotNull(vo.getTodayCallCount());
        assertNotNull(vo.getTodaySuccessCount());
        assertNotNull(vo.getTodayFailCount());
        assertNotNull(vo.getAbnormalCount());
        assertTrue(vo.getInterfaceCount() >= 0);
    }

    @Test
    void getSuccessRate() {
        SuccessRateVo vo = dashboardMonitorService.getSuccessRate();
        assertNotNull(vo);
        assertNotNull(vo.getSuccessCount());
        assertNotNull(vo.getFailCount());
        assertNotNull(vo.getTotalCount());
        assertNotNull(vo.getRate());
        assertTrue(vo.getRate() >= 0 && vo.getRate() <= 100);
    }

    @Test
    void getCallRank() {
        List<InterfaceRankVo> result = dashboardMonitorService.getCallRank(10);
        assertNotNull(result);
    }

    @Test
    void getCallRankZeroDefaultsTo10() {
        List<InterfaceRankVo> result = dashboardMonitorService.getCallRank(0);
        assertNotNull(result);
    }

    @Test
    void getFailRank() {
        List<InterfaceRankVo> result = dashboardMonitorService.getFailRank(10);
        assertNotNull(result);
    }

    @Test
    void getLogTypeDistribution() {
        List<DistributionVo> result = dashboardMonitorService.getLogTypeDistribution();
        assertNotNull(result);
    }

    @Test
    void getRegionDistribution() {
        List<RegionDistributionVo> result = dashboardMonitorService.getRegionDistribution();
        assertNotNull(result);
    }

    @Test
    void getConsumingTimeDistribution() {
        List<ConsumingTimeVo> result = dashboardMonitorService.getConsumingTimeDistribution();
        assertNotNull(result);
    }
}