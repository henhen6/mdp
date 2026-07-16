package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 排行榜数据 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "排行榜数据")
public class RankVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "数值")
    private Long value;
}
