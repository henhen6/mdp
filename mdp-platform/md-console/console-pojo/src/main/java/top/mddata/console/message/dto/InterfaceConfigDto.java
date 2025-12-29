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
 * @since 2025-12-21 00:30:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "接口Dto")
public class InterfaceConfigDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;
    /**
     * 接口标识
     */
    @NotEmpty(message = "请填写接口标识")
    @Size(max = 255, message = "接口标识长度不能超过{max}")
    @Schema(description = "接口标识")
    private String key;

    /**
     * 接口名称
     */
    @NotEmpty(message = "请填写接口名称")
    @Size(max = 255, message = "接口名称长度不能超过{max}")
    @Schema(description = "接口名称")
    private String name;
    /**
     * 接口类型
     * [1-短信 2-邮件 3-微信]
     */
    @NotNull(message = "请填写接口类型")
    @Schema(description = "接口类型")
    private Integer msgType;
    /**
     * 执行方式
     * [1-实现类 2-脚本 3-magic-api]
     */
    @NotNull(message = "请填写执行方式")
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

}
