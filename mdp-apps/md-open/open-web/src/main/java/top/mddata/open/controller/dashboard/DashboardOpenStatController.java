package top.mddata.open.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.open.service.dashboard.DashboardOpenService;

import java.util.Map;

/**
 * 开放平台大屏统计 远程调用接口
 *
 * @author henhen6
 * @since 2026-07-19
 */
@RestController
@Tag(name = "开放平台大屏统计-RPC接口")
@RequestMapping("/dashboard/open/stat")
@RequiredArgsConstructor
public class DashboardOpenStatController {

    private final DashboardOpenService dashboardOpenService;

    /**
     * 获取各项业务成功率统计（供其他服务RPC调用）
     *
     * @return key=successCount、totalCount
     *         分别包含 callback、apiCall、eventPush 三个业务类型
     */
    @GetMapping("/success-rates")
    @Operation(summary = "获取各项业务成功率统计", description = "供其他服务RPC调用，返回回调、API调用、事件通知的成功率统计")
    public R<Map<String, Map<String, Long>>> getSuccessRates() {
        return R.success(dashboardOpenService.getSuccessRates());
    }
}
