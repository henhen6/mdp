package top.mddata.open.admin.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.open.admin.entity.base.AppGroupRelBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用拥有的权限分组 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用拥有的权限分组")
@Table(AppGroupRelBase.TABLE_NAME)
public class AppGroupRelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属应用
     */
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 分组
     * perm_group.id
     */
    @Schema(description = "分组")
    private Long groupId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 创建人id
     */
    @Schema(description = "创建人id")
    private Long createdBy;

}
