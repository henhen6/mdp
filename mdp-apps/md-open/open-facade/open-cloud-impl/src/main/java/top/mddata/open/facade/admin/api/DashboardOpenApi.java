package top.mddata.open.facade.admin.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;

import java.util.Map;

/**
 * 开放平台大屏统计 API（供其他服务RPC调用）
 *
 * @author henhen6
 * @since 2026-07-19
 */
@FeignClient(name = AppConstants.OPEN_SERVER)
public interface DashboardOpenApi {

    /**
     * 获取各项业务成功率统计
     *
     * @return key=successCount、totalCount
     *         分别包含 interface、callback、apiCall、eventPush 四个业务类型
     */
    @GetMapping("/dashboard/open/stat/success-rates")
    R<Map<String, Map<String, Long>>> getSuccessRates();
}
