package top.mddata.console.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 接口 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:47
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "接口Dto")
public class InterfaceConfigDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "请填写", groups = BaseEntity.Update.class)
    @Schema(description = "")
    private Long id;

    /**
     * 接口名称
     */
    @NotEmpty(message = "请填写接口名称")
    @Size(max = 255, message = "接口名称长度不能超过{max}")
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
    @Size(max = 16383, message = "实现脚本长度不能超过{max}")
    @Schema(description = "实现脚本")
    private String script;

    /**
     * 实现类
     */
    @Size(max = 255, message = "实现类长度不能超过{max}")
    @Schema(description = "实现类")
    private String implClass;

    /**
     * 实现ID
     */
    @Size(max = 255, message = "实现ID长度不能超过{max}")
    @Schema(description = "实现ID")
    private String magicApiId;

    /**
     * 状态
     */
    @NotNull(message = "请填写状态")
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 配置参数
     * (JSON存储：AppId, SecretKey等)
     */
    @Size(max = 536870911, message = "配置参数长度不能超过{max}")
    @Schema(description = "配置参数")
    private String configJson;

}
