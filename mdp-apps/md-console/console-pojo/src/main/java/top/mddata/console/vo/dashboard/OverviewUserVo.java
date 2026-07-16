package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户与组织概览统计 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "用户与组织概览统计")
public class OverviewUserVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户总数")
    private Long userCount;

    @Schema(description = "单位数量")
    private Long companyCount;

    @Schema(description = "部门数量")
    private Long deptCount;

    @Schema(description = "角色数量")
    private Long roleCount;
}
