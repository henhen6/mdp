package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 接口成功率 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "接口成功率")
public class SuccessRateVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "成功次数")
    private Long successCount;

    @Schema(description = "失败次数")
    private Long failCount;

    @Schema(description = "总次数")
    private Long totalCount;

    @Schema(description = "成功率（百分比）")
    private BigDecimal rate;
}