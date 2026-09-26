package top.mddata.console.vo.permission;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.console.entity.permission.base.RoleDataScopeRelBase;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色数据范围授权 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色数据范围授权")
@Table(RoleDataScopeRelBase.TABLE_NAME)
public class RoleDataScopeRelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属角色
     */
    @Schema(description = "所属角色")
    private Long roleId;

    /**
     * 所属菜单
     */
    @Schema(description = "所属菜单")
    private Long menuId;

    /**
     * 数据范围（编码见 DataScopeEnum：10/20/30/40/50/90）
     */
    @Schema(description = "数据范围")
    private String dataScope;

    /**
     * 自定义实现的 Spring Bean 名（仅 dataScope=90 时非空）
     */
    @Schema(description = "自定义实现的 Spring Bean 名")
    private String dataScopeImpl;
}
