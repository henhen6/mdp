package top.mddata.console.dto.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 编码规则单号生成 DTO（单个/同参批量的入参）。
 */
@Accessors(chain = true)
@Data
@Schema(description = "编码规则单号生成")
public class CodeGenerateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规则编码
     */
    @NotBlank(message = "请填写规则编码")
    @Schema(description = "规则编码")
    private String ruleCode;

    /**
     * 变量参数（key 为变量名）
     */
    @Schema(description = "变量参数")
    private Map<String, String> params;

    /**
     * 生成数量
     */
    @NotNull(message = "请填写生成数量")
    @Min(value = 1, message = "生成数量不能小于{value}")
    @Max(value = 1000, message = "生成数量不能大于{value}")
    @Schema(description = "生成数量")
    private Integer count;

}