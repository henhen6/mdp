package top.mddata.open.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.open.vo.dashboard.ApiRankVo;
import top.mddata.open.vo.dashboard.AppRankVo;
import top.mddata.open.vo.dashboard.CallTrendVo;
import top.mddata.open.vo.dashboard.EventPushStatisticsVo;
import top.mddata.open.vo.dashboard.EventPushTrendVo;
import top.mddata.open.vo.dashboard.EventTriggerStatisticsVo;
import top.mddata.open.vo.dashboard.EventTriggerTrendVo;
import top.mddata.open.vo.dashboard.OauthDistributionVo;
import top.mddata.open.vo.dashboard.OverviewOpenVo;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DashboardOpenService 测试
 *
 * @author henhen6
 * @since 2026-07-10
 */
@SpringBootTest
class DashboardOpenServiceTest {

    @Autowired
    private DashboardOpenService dashboardOpenService;

    @Test
    void getOverviewOpen() {
        OverviewOpenVo result = dashboardOpenService.getOverviewOpen();

        assertNotNull(result);
        assertNotNull(result.getAppCount());
        assertNotNull(result.getSelfBuildCount());
        assertNotNull(result.getThirdPartyCount());
        assertNotNull(result.getApiCount());
        assertNotNull(result.getTodayApiCallCount());
        assertNotNull(result.getTodayFailCount());
        assertNotNull(result.getPendingApplyCount());
        assertTrue(result.getAppCount() >= 0);
        assertTrue(result.getTodayApiCallCount() >= 0);
    }

    @Test
    void getCallTrend7Days() {
        List<CallTrendVo> result = dashboardOpenService.getCallTrend(7);
        assertNotNull(result);
        assertEquals(7, result.size());
    }

    @Test
    void getCallTrend30Days() {
        List<CallTrendVo> result = dashboardOpenService.getCallTrend(30);
        assertNotNull(result);
        assertEquals(30, result.size());
    }

    @Test
    void getCallTrendIllegalDaysDefaultsTo7() {
        List<CallTrendVo> result = dashboardOpenService.getCallTrend(15);
        assertNotNull(result);
        assertEquals(7, result.size());
    }

    @Test
    void getAppRank() {
        List<AppRankVo> result = dashboardOpenService.getAppRank(10);
        assertNotNull(result);
    }

    @Test
    void getAppRankZeroDefaultsTo10() {
        List<AppRankVo> result = dashboardOpenService.getAppRank(0);
        assertNotNull(result);
    }

    @Test
    void getApiRank() {
        List<ApiRankVo> result = dashboardOpenService.getApiRank(10);
        assertNotNull(result);
    }

    @Test
    void getOauthDistribution() {
        List<OauthDistributionVo> result = dashboardOpenService.getOauthDistribution();
        assertNotNull(result);
    }

    @Test
    void getEventTriggerStatistics() {
        List<EventTriggerStatisticsVo> result = dashboardOpenService.getEventTriggerStatistics();
        assertNotNull(result);
    }

    @Test
    void getEventTriggerTrendDefaultRange() {
        List<EventTriggerTrendVo> result = dashboardOpenService.getEventTriggerTrend(null, null);
        assertNotNull(result);
        assertEquals(7, result.size());
    }

    @Test
    void getEventTriggerTrendCustomRange() {
        LocalDate start = LocalDate.now().minusDays(14);
        LocalDate end = LocalDate.now().minusDays(2);
        List<EventTriggerTrendVo> result = dashboardOpenService.getEventTriggerTrend(start, end);
        assertNotNull(result);
        assertEquals(13, result.size());
    }

    @Test
    void getEventTriggerRank() {
        List<EventTriggerStatisticsVo> result = dashboardOpenService.getEventTriggerRank(10);
        assertNotNull(result);
    }

    @Test
    void getEventPushStatistics() {
        List<EventPushStatisticsVo> result = dashboardOpenService.getEventPushStatistics();
        assertNotNull(result);
    }

    @Test
    void getEventPushTrendDefaultRange() {
        List<EventPushTrendVo> result = dashboardOpenService.getEventPushTrend(null, null);
        assertNotNull(result);
        assertEquals(7, result.size());
    }
}