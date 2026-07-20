package top.mddata.open.facade.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.facade.admin.DashboardOpenFacade;
import top.mddata.open.facade.admin.api.DashboardOpenApi;

import java.util.Map;

/**
 * 开放平台大屏统计 Facade实现（云服务版本，调用远程API）
 *
 * @author henhen6
 * @since 2026-07-19
 */
@Service
@RequiredArgsConstructor
public class DashboardOpenFacadeImpl implements DashboardOpenFacade {

    private final DashboardOpenApi dashboardOpenApi;

    @Override
    public R<Map<String, Map<String, Long>>> getSuccessRates() {
        return dashboardOpenApi.getSuccessRates();
    }
}
