package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 请求接口排行 VO
 *
 * <p>前端显示 httpUri，hover 显示完整信息：classPath.methodName(httpUri httpMethod)(description)</p>
 *
 * @author henhen6
 * @since 2026-07-18
 */
@Data
@Schema(description = "请求接口排行")
public class RequestInterfaceRankVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "接口唯一标识（classPath.methodName）")
    private String interfaceName;

    @Schema(description = "前端显示：请求地址")
    private String httpUri;

    @Schema(description = "HTTP请求方法")
    private String httpMethod;

    @Schema(description = "接口描述")
    private String description;

    @Schema(description = "请求次数")
    private Long count;

    @Schema(description = "hover显示完整信息：classPath.methodName(httpUri httpMethod)(description)")
    private String fullName;
}
