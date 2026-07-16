package top.mddata.open.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * API调用趋势 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "API调用趋势")
public class CallTrendVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "日期")
    private String date;

    @Schema(description = "调用总数")
    private Long callCount;

    @Schema(description = "失败数")
    private Long failCount;
}