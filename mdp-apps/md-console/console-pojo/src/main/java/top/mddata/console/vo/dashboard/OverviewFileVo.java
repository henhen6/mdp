package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件存储概览统计 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "文件存储概览统计")
public class OverviewFileVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "文件总数")
    private Long fileCount;

    @Schema(description = "文件总容量（字节）")
    private Long fileTotalSize;

    @Schema(description = "本月新增文件数")
    private Long monthFileCount;

    @Schema(description = "本月新增容量（字节）")
    private Long monthTotalSize;

    @Schema(description = "临时文件数量")
    private Long tempFileCount;

    @Schema(description = "临时文件容量（字节）")
    private Long tempFileSize;
}
