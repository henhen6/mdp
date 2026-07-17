package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 请求日志概览 VO
 *
 * @author henhen6
 * @since 2026-07-18
 */
@Data
@Schema(description = "请求日志概览")
public class OverviewRequestVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "总请求量")
    private Long totalCount;

    @Schema(description = "异常请求数量")
    private Long abnormalCount;

    @Schema(description = "成功请求数量")
    private Long successCount;
}
