package top.mddata.console.dto.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 编码规则 DTO（写入方法入参）。
 */
@Accessors(chain = true)
@Data
@Schema(description = "编码规则")
public class CodeRuleDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 规则编码（唯一）
     */
    @NotBlank(message = "请填写规则编码")
    @Size(max = 64, message = "规则编码长度不能超过{max}")
    @Schema(description = "规则编码")
    private String code;

    /**
     * 规则名称
     */
    @NotBlank(message = "请填写规则名称")
    @Size(max = 128, message = "规则名称长度不能超过{max}")
    @Schema(description = "规则名称")
    private String name;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过{max}")
    @Schema(description = "备注")
    private String remark;

    /**
     * 状态
     */
    @NotNull(message = "请填写状态")
    @Schema(description = "状态")
    private Boolean state;

}
