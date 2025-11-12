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
 * 接口权限 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:29
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "接口权限")
public class ResourceUriDto implements Serializable {

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
     * 所属应用
     */
    @NotEmpty(message = "请填写所属应用")
    @Size(max = 255, message = "所属应用长度不能超过{max}")
    @Schema(description = "所属应用")
    private String applicationName;

    /**
     * 类名
     */
    @NotEmpty(message = "请填写类名")
    @Size(max = 255, message = "类名长度不能超过{max}")
    @Schema(description = "类名")
    private String controller;

    /**
     * 接口名
     */
    @NotEmpty(message = "请填写接口名")
    @Size(max = 255, message = "接口名长度不能超过{max}")
    @Schema(description = "接口名")
    private String name;

    /**
     * 接口地址
     */
    @Size(max = 255, message = "接口地址长度不能超过{max}")
    @Schema(description = "接口地址")
    private String uri;

    /**
     * 请求方式
     */
    @Size(max = 255, message = "请求方式长度不能超过{max}")
    @Schema(description = "请求方式")
    private String requestMethod;

    /**
     * 手动录入
     */
    @Schema(description = "手动录入")
    private Boolean isInput;

}
