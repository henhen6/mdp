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
 * 开放平台大屏统计 远程调用接口（仅供平台内部服务经 Feign 调用）
 *
 * @author henhen6
 * @since 2026-07-19
 */
@RestController
@Tag(name = "开放平台大屏统计-RPC接口")
@RequestMapping("/inner/dashboard/open/stat")
@RequiredArgsConstructor
public class DashboardOpenStatController {

    private final DashboardOpenService dashboardOpenService;

    /**
     * 获取各项业务成功率统计
     *
     * @return key=successCount、totalCount
     *         分别包含 interface、callback、apiCall、eventPush 四个业务类型
     */
    @Operation(hidden = true)
    @GetMapping("/successRates")
    public R<Map<String, Map<String, Long>>> getSuccessRates() {
        return R.success(dashboardOpenService.getSuccessRates());
    }
}
