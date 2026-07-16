package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件增长趋势 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "文件增长趋势")
public class FileTrendVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "日期")
    private String date;

    @Schema(description = "新增文件数")
    private Long fileCount;

    @Schema(description = "新增容量（字节）")
    private Long totalSize;
}
