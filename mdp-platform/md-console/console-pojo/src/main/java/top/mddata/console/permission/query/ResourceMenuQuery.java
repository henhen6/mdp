package top.mddata.console.permission.query;

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
 * 菜单 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜单")
public class ResourceMenuQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属应用
     */
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 编码
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 类型
     * [10-目录  20-菜单 30-内链 40-外链]
     */
    @Schema(description = "类型")
    private String menuType;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remarks;

    /**
     * 路由地址
     */
    @Schema(description = "路由地址")
    private String path;

    /**
     * 页面地址
     */
    @Schema(description = "页面地址")
    private String component;

    /**
     * 重定向
     */
    @Schema(description = "重定向")
    private String redirect;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 树路径
     */
    @Schema(description = "树路径")
    private String treePath;

    /**
     * 父级ID
     */
    @Schema(description = "父级ID")
    private Long parentId;

    /**
     * 顺序号
     */
    @Schema(description = "顺序号")
    private Integer weight;

    /**
     * 元数据
     *
     */
    @Schema(description = "元数据")
    private String meta;

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
