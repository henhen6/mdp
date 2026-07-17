package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * IP地址请求排行 VO
 *
 * @author henhen6
 * @since 2026-07-18
 */
@Data
@Schema(description = "IP地址请求排行")
public class IpRankVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "请求次数")
    private Long count;
}
