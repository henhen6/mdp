package top.mddata.open.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * API调用排行 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "API调用排行")
public class ApiRankVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "API ID")
    private Long apiId;

    @Schema(description = "API名称")
    private String apiName;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "调用次数")
    private Long callCount;
}