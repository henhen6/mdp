package top.mddata.open.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 事件推送趋势 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "事件推送趋势")
public class EventPushTrendVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "日期")
    private String date;

    @Schema(description = "触发次数")
    private Long triggerCount;

    @Schema(description = "推送请求次数")
    private Long pushCount;
}