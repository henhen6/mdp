package top.mddata.console.permission.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 拥有拥有的权限资源
 *
 * @author henhen6
 * @date 2020/11/28 12:15 下午
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "VisibleResourceVO", description = "权限资源")
public class VisibleResourceVO implements Serializable {
    @Schema(description = "是否启用接口/按钮权限")
    private Boolean enabled;
    @Schema(description = "按钮权限是否区分大小写")
    private Boolean caseSensitive;
    @Schema(description = "拥有的资源编码")
    private List<String> resourceList;
    @Schema(description = "拥有的角色编码")
    private List<String> roleList;
}
