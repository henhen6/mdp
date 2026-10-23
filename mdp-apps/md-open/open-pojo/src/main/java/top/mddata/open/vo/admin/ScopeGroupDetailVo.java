package top.mddata.open.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 权限组详情（含组内开放接口与 oauth2 权限）。
 *
 * @author henhen6
 */
@Data
@Accessors(chain = true)
@Schema(description = "权限组详情")
public class ScopeGroupDetailVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "排序")
    private Long weight;

    @Schema(description = "组内开放接口")
    private List<ApiVo> apis;

    @Schema(description = "组内oauth2权限")
    private List<OauthScopeVo> scopes;
}
