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
 * 按钮 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "按钮")
public class ResourceButtonDto implements Serializable {

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
     * 图标
     */
    @Size(max = 255, message = "图标长度不能超过{max}")
    @Schema(description = "图标")
    private String icon;

    /**
     * 显示方式
     *  [01-文字 02-图标 03-文字和图标]
     */
    @Size(max = 2, message = "显示方式长度不能超过{max}")
    @Schema(description = "显示方式")
    private String showMode;

    /**
     * 按钮类型 
     * [01-default 02-primary 03-info 04-success 05-warning 06-error]
     */
    @Size(max = 2, message = "按钮类型 长度不能超过{max}")
    @Schema(description = "按钮类型 ")
    private String btnType;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    @NotNull(message = "请填写状态")
    @Schema(description = "状态")
    private Boolean state;

}
