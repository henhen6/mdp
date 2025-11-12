package top.mddata.console.permission.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.console.permission.entity.base.RoleResourceRelBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色资源关联 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:29
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色资源关联")
@Table(RoleResourceRelBase.TABLE_NAME)
public class RoleResourceRelVo implements Serializable {

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
     * 资源类型
     */
    @Schema(description = "资源类型")
    private String resourceType;

    /**
     * 所属资源
     */
    @Schema(description = "所属资源")
    private Long resourceId;

    /**
     * 创建人id
     */
    @Schema(description = "创建人id")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

}
