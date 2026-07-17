package top.mddata.open.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 开放平台概览统计 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "开放平台概览统计")
public class OverviewOpenVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用总数（启用状态）")
    private Long appCount;

    @Schema(description = "自建应用数")
    private Long selfBuildCount;

    @Schema(description = "第三方应用数")
    private Long thirdPartyCount;

    @Schema(description = "API总数（启用状态）")
    private Long apiCount;

    @Schema(description = "今日API调用量")
    private Long todayApiCallCount;

    @Schema(description = "今日调用失败数")
    private Long todayFailCount;

    @Schema(description = "应用待审批数")
    private Long pendingApplyCount;

    @Schema(description = "应用申请退回数")
    private Long rejectedApplyCount;
}