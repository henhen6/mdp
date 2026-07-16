package top.mddata.open.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 事件应用推送统计 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "事件应用推送统计")
public class EventPushStatisticsVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "事件编码")
    private String eventCode;

    @Schema(description = "事件名称")
    private String eventName;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "触发次数")
    private Long triggerCount;

    @Schema(description = "推送次数")
    private Long pushCount;
}