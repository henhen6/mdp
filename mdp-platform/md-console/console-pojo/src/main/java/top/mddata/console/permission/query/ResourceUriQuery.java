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
 * 接口权限 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:29
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "接口权限")
public class ResourceUriQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属菜单
     */
    @Schema(description = "所属菜单")
    private Long menuId;

    /**
     * 所属应用
     */
    @Schema(description = "所属应用")
    private String applicationName;

    /**
     * 类名
     */
    @Schema(description = "类名")
    private String controller;

    /**
     * 接口名
     */
    @Schema(description = "接口名")
    private String name;

    /**
     * 接口地址
     */
    @Schema(description = "接口地址")
    private String uri;

    /**
     * 请求方式
     */
    @Schema(description = "请求方式")
    private String requestMethod;

    /**
     * 手动录入
     */
    @Schema(description = "手动录入")
    private Boolean isInput;

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

}
