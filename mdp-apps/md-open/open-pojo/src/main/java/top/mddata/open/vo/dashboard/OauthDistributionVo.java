package top.mddata.open.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * OAuth授权分布 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "OAuth授权分布")
public class OauthDistributionVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "授权类型")
    private String grantType;

    @Schema(description = "授权次数")
    private Long count;

    @Schema(description = "占比（百分比）")
    private BigDecimal percent;
}