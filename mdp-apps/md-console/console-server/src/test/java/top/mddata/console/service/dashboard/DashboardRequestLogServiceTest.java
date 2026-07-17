package top.mddata.console.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.console.vo.dashboard.ConsumingTimeVo;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.RegionDistributionVo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DashboardRequestLogService 测试
 *
 * @author henhen6
 * @since 2026-07-17
 */
@SpringBootTest
class DashboardRequestLogServiceTest {

    @Autowired
    private DashboardRequestLogService dashboardRequestLogService;

    @Test
    void getLogTypeDistribution() {
        List<DistributionVo> result = dashboardRequestLogService.getLogTypeDistribution();
        assertNotNull(result);
    }

    @Test
    void getRegionDistribution() {
        List<RegionDistributionVo> result = dashboardRequestLogService.getRegionDistribution();
        assertNotNull(result);
    }

    @Test
    void getConsumingTimeDistribution() {
        List<ConsumingTimeVo> result = dashboardRequestLogService.getConsumingTimeDistribution();
        assertNotNull(result);
    }

    @Test
    void getAbnormalCount() {
        Long result = dashboardRequestLogService.getAbnormalCount();
        assertNotNull(result);
    }
}
