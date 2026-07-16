package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息通知概览统计 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "消息通知概览统计")
public class OverviewMessageVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息任务总数")
    private Long msgCount;

    @Schema(description = "今日发送消息数（执行成功）")
    private Long todaySendCount;

    @Schema(description = "待执行消息数")
    private Long pendingCount;
}
