package top.mddata.console.query.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-12-01 00:12:36
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色")
public class RoleQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码")
    private String code;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String name;

    /**
     * 角色分类
     * [10-普通角色 20-管理员角色 30-权限集合]
     */
    @Schema(description = "角色分类")
    private String roleCategory;

    /**
     * 组织性质
     * [1-总公司 90-开发者 99-运营]
     */
    @Schema(description = "组织性质")
    private Integer orgNature;

    /**
     * 是否模版
     */
    @Schema(description = "是否模版")
    private Boolean templateRole;

    /**
     * 说明
     */
    @Schema(description = "说明")
    private String remarks;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    @Schema(description = "状态")
    private Boolean state;

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

    /**
     * 更新人id
     */
    @Schema(description = "更新人id")
    private Long updatedBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 删除人
     */
    @Schema(description = "删除人")
    private Long deletedBy;

    /**
     * 删除标志
     */
    @Schema(description = "删除标志")
    private Long deletedAt;

}
