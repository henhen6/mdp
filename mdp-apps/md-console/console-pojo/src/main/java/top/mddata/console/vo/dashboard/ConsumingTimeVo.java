package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 请求耗时分布 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "请求耗时分布")
public class ConsumingTimeVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "耗时区间标签")
    private String name;

    @Schema(description = "请求次数")
    private Long count;
}