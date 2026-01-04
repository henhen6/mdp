package top.mddata.open.admin.dto;

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
 * 事件类型 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "事件类型Dto")
public class EventTypeDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "请填写主键", groups = BaseEntity.Update.class)
    @Schema(description = "主键")
    private Long id;

    /**
     * 事件编码
     */
    @NotEmpty(message = "请填写事件编码")
    @Size(max = 255, message = "事件编码长度不能超过{max}")
    @Schema(description = "事件编码")
    private String code;

    /**
     * 事件名称
     */
    @NotEmpty(message = "请填写事件名称")
    @Size(max = 255, message = "事件名称长度不能超过{max}")
    @Schema(description = "事件名称")
    private String name;

    /**
     * 状态
     */
    @Schema(description = "状态")
    @NotNull(message = "请填写状态")
    private Boolean state;

}
