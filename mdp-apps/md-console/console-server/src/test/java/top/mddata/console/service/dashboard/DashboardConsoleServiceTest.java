package top.mddata.console.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.console.vo.dashboard.OverviewConsoleVo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DashboardConsoleService 测试
 *
 * @author henhen6
 * @since 2026-07-10
 */
@SpringBootTest
class DashboardConsoleServiceTest {

    @Autowired
    private DashboardConsoleService dashboardConsoleService;

    @Test
    void getOverviewConsole() {
        OverviewConsoleVo result = dashboardConsoleService.getOverviewConsole();

        assertNotNull(result);
        assertNotNull(result.getUserCount());
        assertNotNull(result.getOrgCount());
        assertNotNull(result.getFileCount());
        assertTrue(result.getUserCount() >= 0);
        assertTrue(result.getOrgCount() >= 0);
        assertTrue(result.getFileCount() >= 0);
    }
}
