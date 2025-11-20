package top.mddata.open.admin.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.open.admin.entity.base.GroupApiRelBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分组拥有的对外接口 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分组拥有的对外接口")
@Table(GroupApiRelBase.TABLE_NAME)
public class GroupApiRelVo implements Serializable {

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
     * perm_group.id
     */
    @Schema(description = "所属分组")
    private Long groupId;

    /**
     * 所属文档
     * api_info.id
     */
    @Schema(description = "所属文档")
    private Long apiId;

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
