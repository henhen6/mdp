package top.mddata.console.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.FileTrendVo;
import top.mddata.console.vo.dashboard.OverviewFileVo;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DashboardFileService 测试
 *
 * @author henhen6
 * @since 2026-07-10
 */
@SpringBootTest
class DashboardFileServiceTest {

    @Autowired
    private DashboardFileService dashboardFileService;

    @Test
    void getOverviewFile() {
        OverviewFileVo vo = dashboardFileService.getOverviewFile();
        assertNotNull(vo);
        assertNotNull(vo.getFileCount());
        assertNotNull(vo.getFileTotalSize());
        assertNotNull(vo.getMonthFileCount());
        assertNotNull(vo.getMonthTotalSize());
    }

    @Test
    void getFileTypeDistribution() {
        List<DistributionVo> result = dashboardFileService.getFileTypeDistribution();
        assertNotNull(result);
    }

    @Test
    void getObjectTypeDistribution() {
        List<DistributionVo> result = dashboardFileService.getObjectTypeDistribution();
        assertNotNull(result);
    }

    @Test
    void getPlatformDistribution() {
        List<DistributionVo> result = dashboardFileService.getPlatformDistribution();
        assertNotNull(result);
    }

    @Test
    void getSizeDistribution() {
        List<DistributionVo> result = dashboardFileService.getSizeDistribution();
        assertNotNull(result);
    }

    @Test
    void getTrend7Days() {
        List<FileTrendVo> result = dashboardFileService.getTrend(null, null);
        assertNotNull(result);
        assertEquals(7, result.size());
    }

    @Test
    void getTrend30Days() {
        LocalDate start = LocalDate.of(2026, 6, 17);
        LocalDate end = LocalDate.of(2026, 7, 17);
        List<FileTrendVo> result = dashboardFileService.getTrend(start, end);
        assertNotNull(result);
        assertEquals(31, result.size());
    }

    @Test
    void getTrendIllegalDaysDefaultsTo7() {
        LocalDate start = LocalDate.of(2026, 7, 10);
        LocalDate end = LocalDate.of(2026, 7, 16);
        List<FileTrendVo> result = dashboardFileService.getTrend(start, end);
        assertNotNull(result);
        assertTrue(result.size() == 7);
    }
}
