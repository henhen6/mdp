package top.mddata.open.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.open.entity.base.GroupScopeRelBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分组拥有的oauth2权限 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分组拥有的oauth2权限")
@Table(GroupScopeRelBase.TABLE_NAME)
public class GroupScopeRelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private Long id;

    /**
     * 所属分组
     */
    @Schema(description = "所属分组")
    private Long groupId;

    /**
     * 所属权限
     */
    @Schema(description = "所属权限")
    private Long scopeId;

    /**
     * 添加时间
     */
    @Schema(description = "添加时间")
    private LocalDateTime createdAt;

    /**
     * 创建人id
     */
    @Schema(description = "创建人id")
    private Long createdBy;

}
