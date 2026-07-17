package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息发送趋势数据 VO（支持多类型趋势线）
 *
 * @author henhen6
 * @since 2026-07-16
 */
@Data
@Schema(description = "消息发送趋势数据")
public class TrendLineVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "日期，格式 yyyy-MM-dd")
    private String date;

    @Schema(description = "站内信数量")
    private Long noticeCount;

    @Schema(description = "短信数量")
    private Long smsCount;

    @Schema(description = "邮件数量")
    private Long mailCount;

    @Schema(description = "总量")
    private Long totalCount;
}
