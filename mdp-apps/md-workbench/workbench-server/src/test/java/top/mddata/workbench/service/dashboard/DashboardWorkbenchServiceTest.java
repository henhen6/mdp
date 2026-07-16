package top.mddata.workbench.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.workbench.vo.dashboard.OverviewWorkbenchVo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DashboardWorkbenchService 测试
 *
 * @author henhen6
 * @since 2026-07-10
 */
@SpringBootTest
class DashboardWorkbenchServiceTest {

    @Autowired
    private DashboardWorkbenchService dashboardWorkbenchService;

    @Test
    void getOverviewWorkbench() {
        OverviewWorkbenchVo result = dashboardWorkbenchService.getOverviewWorkbench();

        assertNotNull(result);
        assertNotNull(result.getTodayLoginCount());
        assertTrue(result.getTodayLoginCount() >= 0);
    }
}