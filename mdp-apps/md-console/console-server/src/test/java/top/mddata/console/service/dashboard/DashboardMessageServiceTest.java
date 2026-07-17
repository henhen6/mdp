package top.mddata.console.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewMessageVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendLineVo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DashboardMessageService 测试
 *
 * @author henhen6
 * @since 2026-07-10
 */
@SpringBootTest
class DashboardMessageServiceTest {

    @Autowired
    private DashboardMessageService dashboardMessageService;

    @Test
    void getOverviewMessage() {
        OverviewMessageVo vo = dashboardMessageService.getOverviewMessage();
        assertNotNull(vo);
        assertNotNull(vo.getMsgCount());
        assertNotNull(vo.getTodaySendCount());
        assertNotNull(vo.getPendingCount());
    }

    @Test
    void getTypeDistribution() {
        List<DistributionVo> result = dashboardMessageService.getTypeDistribution();
        assertNotNull(result);
    }

    @Test
    void getCategoryDistribution() {
        List<DistributionVo> result = dashboardMessageService.getCategoryDistribution();
        assertNotNull(result);
    }

    @Test
    void getTrend7Days() {
        List<TrendLineVo> result = dashboardMessageService.getTrend(null, null, null);
        assertNotNull(result);
        assertEquals(7, result.size());
    }

    @Test
    void getTrend30Days() {
        List<TrendLineVo> result = dashboardMessageService.getTrend("2026-06-17", "2026-07-17", null);
        assertNotNull(result);
        assertEquals(31, result.size());
    }

    @Test
    void getTemplateRank() {
        List<RankVo> result = dashboardMessageService.getTemplateRank(10);
        assertNotNull(result);
        assertTrue(result.size() <= 10);
    }
}
