package top.mddata.workbench.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 地域分布 VO（用于登录地图）
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "地域分布")
public class RegionDistributionVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "登录次数")
    private Long count;
}
