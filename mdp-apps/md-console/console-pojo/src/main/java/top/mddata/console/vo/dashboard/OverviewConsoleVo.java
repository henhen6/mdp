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

    @Schema(description = "本月新增用户数")
    private Long userNewCount;

    @Schema(description = "待办通知数")
    private Long todoNoticeCount;

    @Schema(description = "公告通知数")
    private Long announcementNoticeCount;

    @Schema(description = "预警通知数")
    private Long warningNoticeCount;

    @Schema(description = "站内通知未读数")
    private Long unreadNoticeCount;

    @Schema(description = "文件总数")
    private Long fileCount;

    @Schema(description = "文件总容量(字节)")
    private Long fileTotalSize;

    @Schema(description = "临时文件占用率(百分比)")
    private Double tempFileRate;

    @Schema(description = "消息成功率(百分比)")
    private Double messageSuccessRate;

    @Schema(description = "接口成功率(百分比)")
    private Double interfaceSuccessRate;

    @Schema(description = "回调成功率(百分比)")
    private Double callbackSuccessRate;

    @Schema(description = "API调用成功率(百分比)")
    private Double apiCallSuccessRate;

    @Schema(description = "事件通知成功率(百分比)")
    private Double eventPushSuccessRate;
}
