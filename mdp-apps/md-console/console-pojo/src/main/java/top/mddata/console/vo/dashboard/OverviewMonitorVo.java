package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 接口监控概览 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "接口监控概览")
public class OverviewMonitorVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "接口总数")
    private Long interfaceCount;

    @Schema(description = "今日调用总次数")
    private Long todayCallCount;

    @Schema(description = "今日成功次数")
    private Long todaySuccessCount;

    @Schema(description = "今日失败次数")
    private Long todayFailCount;

    @Schema(description = "异常请求数")
    private Long abnormalCount;
}