package top.mddata.open.facade.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.facade.admin.DashboardOpenFacade;
import top.mddata.open.service.dashboard.DashboardOpenService;

import java.util.Map;

/**
 * 开放平台大屏统计 Facade实现
 *
 * @author henhen6
 * @since 2026-07-19
 */
@Service
@RequiredArgsConstructor
public class DashboardOpenFacadeImpl implements DashboardOpenFacade {

    private final DashboardOpenService dashboardOpenService;

    @Override
    public R<Map<String, Map<String, Long>>> getSuccessRates() {
        return R.success(dashboardOpenService.getSuccessRates());
    }
}
