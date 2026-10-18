package top.mddata.open.controller.dashboard;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.open.service.dashboard.DashboardOpenService;

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
}
