package top.mddata.workbench.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录概览统计 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "登录概览统计")
public class OverviewLoginVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "今日登录成功次数")
    private Long todayLoginCount;

    @Schema(description = "今日登录失败次数")
    private Long todayFailCount;
}
