package top.mddata.console.message.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 接口 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Schema(description = "接口Query")
public class InterfaceConfigQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;
    /**
     * 接口标识
     */
    @Schema(description = "接口标识")
    private String key;
    /**
     * 接口名称
     */
    @Schema(description = "接口名称")
    private String name;

    /**
     * 执行方式
     * [1-实现类 2-脚本 3-magic-api]
     */
    @Schema(description = "执行方式")
    private Integer execMode;

    /**
     * 实现脚本
     */
    @Schema(description = "实现脚本")
    private String script;

    /**
     * 实现类
     */
    @Schema(description = "实现类")
    private String implClass;

    /**
     * 实现ID
     */
    @Schema(description = "实现ID")
    private String magicApiId;

    /**
     * 配置参数
     * (JSON存储：AppId, SecretKey等)
     */
    @Schema(description = "配置参数")
    private String configJson;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private Long updatedBy;

}
