package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统概览统计 VO (console部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "系统概览统计(console部分)")
public class OverviewConsoleVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户总数")
    private Long userCount;

    @Schema(description = "组织总数")
    private Long orgCount;

    @Schema(description = "待办通知数")
    private Long todoNoticeCount;

    @Schema(description = "公告通知数")
    private Long announcementNoticeCount;

    @Schema(description = "预警通知数")
    private Long warningNoticeCount;

    @Schema(description = "文件总数")
    private Long fileCount;

    @Schema(description = "文件总容量(字节)")
    private Long fileTotalSize;
}
