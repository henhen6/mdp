package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分布数据 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "分布数据")
public class DistributionVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "数量")
    private Long count;

    @Schema(description = "占比")
    private BigDecimal percent;
}
