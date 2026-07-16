package top.mddata.workbench.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 每日登录统计 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "每日登录统计")
public class DailyLoginVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "日期 (yyyy-MM-dd)")
    private String date;

    @Schema(description = "登录总次数")
    private Long loginCount;

    @Schema(description = "登录人次（去重用户数）")
    private Long userCount;
}
