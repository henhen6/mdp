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
 * 数据权限 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "数据权限")
public class ResourceDataPermDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属菜单
     */
    @NotNull(message = "请填写所属菜单")
    @Schema(description = "所属菜单")
    private Long menuId;

    /**
     * 编码
     * 唯一编码，用于区分资源
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
     * 状态
     * [0-禁用 1-启用]
     */
    @NotNull(message = "请填写状态")
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 默认权限
     */
    @NotNull(message = "请填写默认权限")
    @Schema(description = "默认权限")
    private Boolean isDef;

    /**
     * 数据范围
     * [01-全部 02-本单位及子级 03-本单位 04-本部门及子级 05-本部门 06-个人 07-自定义]
     */
    @NotEmpty(message = "请填写数据范围")
    @Size(max = 2, message = "数据范围长度不能超过{max}")
    @Schema(description = "数据范围")
    private String dataScope;

    /**
     * 实现类
     */
    @Size(max = 255, message = "实现类长度不能超过{max}")
    @Schema(description = "实现类")
    private String customClass;

    /**
     * 顺序号
     */
    @Schema(description = "顺序号")
    private Integer weight;

}
