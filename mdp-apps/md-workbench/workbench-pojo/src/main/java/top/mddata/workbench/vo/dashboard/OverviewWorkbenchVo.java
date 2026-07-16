package top.mddata.workbench.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统概览统计 VO (workbench部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "系统概览统计(workbench部分)")
public class OverviewWorkbenchVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "今日登录次数")
    private Long todayLoginCount;
}