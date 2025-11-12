package top.mddata.console.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜单")
public class ResourceMenuDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属应用
     */
    @NotNull(message = "请填写所属应用")
    @Schema(description = "所属应用")
    private Long applicationId;

    /**
     * 编码
     */
    @NotEmpty(message = "请填写编码")
    @Size(max = 255, message = "编码长度不能超过{max}")
    @Schema(description = "编码")
    private String code;

    /**
     * 名称
     */
    @NotEmpty(message = "请填写名称")
    @Size(max = 255, message = "名称长度不能超过{max}")
    @Schema(description = "名称")
    private String name;

    /**
     * 类型
     * [10-目录  20-菜单 30-内链 40-外链]
     */
    @NotEmpty(message = "请填写类型")
    @Size(max = 2, message = "类型长度不能超过{max}")
    @Schema(description = "类型")
    private String menuType;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过{max}")
    @Schema(description = "备注")
    private String remarks;

    /**
     * 路由地址
     */
    @Size(max = 255, message = "路由地址长度不能超过{max}")
    @Schema(description = "路由地址")
    private String path;

    /**
     * 页面地址
     */
    @Size(max = 255, message = "页面地址长度不能超过{max}")
    @Schema(description = "页面地址")
    private String component;

    /**
     * 重定向
     */
    @Size(max = 255, message = "重定向长度不能超过{max}")
    @Schema(description = "重定向")
    private String redirect;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    @NotNull(message = "请填写状态")
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 树路径
     */
    @Size(max = 1024, message = "树路径长度不能超过{max}")
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
    @Size(max = 1024, message = "元数据长度不能超过{max}")
    @Schema(description = "元数据")
    private String meta;

}
