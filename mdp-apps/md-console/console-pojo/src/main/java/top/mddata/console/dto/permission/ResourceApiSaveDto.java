package top.mddata.console.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 接口权限手动录入 DTO。
 *
 * @author henhen6
 * @since 2026-09-27
 */
@Accessors(chain = true)
@Data
@Schema(description = "接口权限")
public class ResourceApiSaveDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 资源id（菜单id或按钮id）
     */
    @NotNull(message = "资源id不能为空")
    @Schema(description = "资源id（菜单id或按钮id）")
    private Long resourceId;

    /**
     * 接口路径（裸路径，Ant风格通配）
     */
    @NotEmpty(message = "接口路径不能为空")
    @Schema(description = "接口路径（裸路径，Ant风格通配）")
    private String uri;

    /**
     * 请求方式[GET POST PUT DELETE ALL]
     */
    @NotEmpty(message = "请求方式不能为空")
    @Schema(description = "请求方式[GET POST PUT DELETE ALL]")
    private String requestMethod;

    /**
     * 接口名
     */
    @NotEmpty(message = "接口名不能为空")
    @Schema(description = "接口名")
    private String name;
}
