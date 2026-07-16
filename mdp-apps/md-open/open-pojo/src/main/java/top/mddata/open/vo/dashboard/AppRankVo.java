package top.mddata.open.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 应用调用排行 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "应用调用排行")
public class AppRankVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "调用次数")
    private Long callCount;
}