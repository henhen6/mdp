package top.mddata.console.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.console.service.dashboard.DashboardFileService;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.FileTrendVo;
import top.mddata.console.vo.dashboard.OverviewFileVo;

import java.util.List;

/**
 * 文件存储统计 控制层
 *
 * @author henhen6
 * @since 2026-07-10
 */
@RestController
@Tag(name = "大屏统计-文件存储(console)")
@RequestMapping("/dashboard/file")
@RequiredArgsConstructor
public class DashboardFileController {

    private final DashboardFileService dashboardFileService;

    @GetMapping("/overview")
    @Operation(summary = "文件存储概览", description = "文件总数、总容量、本月新增、本月新增容量、临时文件数量、临时文件容量")
    @RequestLog(value = "查询文件存储概览", response = false)
    public R<OverviewFileVo> getOverviewFile() {
        return R.success(dashboardFileService.getOverviewFile());
    }

    @GetMapping("/typeDistribution")
    @Operation(summary = "文件类型分布", description = "目录/图片/文档/视频/音频/其他")
    @RequestLog(value = "查询文件类型分布", response = false)
    public R<List<DistributionVo>> getFileTypeDistribution() {
        return R.success(dashboardFileService.getFileTypeDistribution());
    }

    @GetMapping("/objectTypeDistribution")
    @Operation(summary = "业务类型分布")
    @RequestLog(value = "查询业务类型分布", response = false)
    public R<List<DistributionVo>> getObjectTypeDistribution() {
        return R.success(dashboardFileService.getObjectTypeDistribution());
    }

    @GetMapping("/platformDistribution")
    @Operation(summary = "存储平台分布")
    @RequestLog(value = "查询存储平台分布", response = false)
    public R<List<DistributionVo>> getPlatformDistribution() {
        return R.success(dashboardFileService.getPlatformDistribution());
    }

    @GetMapping("/sizeDistribution")
    @Operation(summary = "文件大小分布", description = "<1MB / 1-10MB / 10-100MB / 100MB-1GB / >=1GB")
    @RequestLog(value = "查询文件大小分布", response = false)
    public R<List<DistributionVo>> getSizeDistribution() {
        return R.success(dashboardFileService.getSizeDistribution());
    }

    @GetMapping("/trend")
    @Operation(summary = "文件增长趋势", description = "按天统计新增文件数与新增容量，支持任意日期区间，默认近7天")
    @RequestLog(value = "查询文件增长趋势", response = false)
    public R<List<FileTrendVo>> getTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return R.success(dashboardFileService.getTrend(startDate, endDate));
    }
}
