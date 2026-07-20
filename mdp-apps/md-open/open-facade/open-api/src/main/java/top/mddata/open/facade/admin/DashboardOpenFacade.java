package top.mddata.open.facade.admin;

import top.mddata.base.base.R;

import java.util.Map;

/**
 * 开放平台大屏统计 Facade接口
 *
 * @author henhen6
 * @since 2026-07-19
 */
public interface DashboardOpenFacade {

    /**
     * 获取各项业务成功率统计
     *
     * @return key=successCount、totalCount
     *         分别包含 interface、callback、apiCall、eventPush 四个业务类型
     */
    R<Map<String, Map<String, Long>>> getSuccessRates();
}
