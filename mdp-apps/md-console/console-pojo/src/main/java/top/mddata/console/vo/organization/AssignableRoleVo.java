package top.mddata.console.vo.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.mddata.console.vo.permission.RoleVo;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 可分配角色 VO（分配角色弹窗聚合数据）。
 *
 * @author henhen6
 * @since 2026-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "可分配角色")
public class AssignableRoleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前操作者是否运营人员（前端决定是否按组织性质分组展示）
     */
    @Schema(description = "当前操作者是否运营人员")
    private boolean operationsAdmin;

    /**
     * 按操作者身份过滤后的可分配角色
     */
    @Schema(description = "可分配角色列表")
    private List<RoleVo> roleList;
}
