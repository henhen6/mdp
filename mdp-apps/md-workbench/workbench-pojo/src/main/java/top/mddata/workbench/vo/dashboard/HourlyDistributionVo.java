package top.mddata.workbench.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 时段分布 VO（按小时统计登录次数）
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "时段分布")
public class HourlyDistributionVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "小时 (0-23)")
    private Integer hour;

    @Schema(description = "登录次数")
    private Long count;
}
