package top.mddata.console.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 接口权限批量绑定 DTO。
 *
 * @author henhen6
 * @since 2026-09-27
 */
@Accessors(chain = true)
@Data
@Schema(description = "接口权限批量绑定")
public class ResourceApiBindDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "资源id不能为空")
    @Schema(description = "资源id（菜单表id）")
    private Long resourceId;

    @Valid
    @NotEmpty(message = "接口列表不能为空")
    @Schema(description = "接口列表")
    private List<ApiItem> apiList;

    @Data
    @Schema(description = "接口项")
    public static class ApiItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @NotEmpty(message = "接口路径不能为空")
        @Schema(description = "接口路径")
        private String uri;

        @NotEmpty(message = "请求方式不能为空")
        @Schema(description = "请求方式")
        private String requestMethod;

        @Schema(description = "接口名")
        private String name;

        @Schema(description = "来源类名")
        private String controller;

        @Schema(description = "来源服务")
        private String applicationName;
    }
}
