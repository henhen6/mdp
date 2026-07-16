package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 接口排行 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "接口排行")
public class InterfaceRankVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "接口ID")
    private Long id;

    @Schema(description = "接口名称")
    private String name;

    @Schema(description = "调用总次数")
    private Long totalCount;

    @Schema(description = "成功次数")
    private Long successCount;

    @Schema(description = "失败次数")
    private Long failCount;
}