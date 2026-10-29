package top.mddata.console.dto.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 编码规则单号生成 DTO（逐参批量的入参）。
 */
@Accessors(chain = true)
@Data
@Schema(description = "编码规则逐参批量生成")
public class CodeGenerateMultiDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规则编码
     */
    @NotBlank(message = "请填写规则编码")
    @Schema(description = "规则编码")
    private String ruleCode;

    /**
     * 变量参数列表（每个元素是一组变量参数，对应生成一个单号）
     */
    @NotEmpty(message = "请填写变量参数列表")
    @Size(max = 1000, message = "变量参数列表长度不能超过{max}")
    @Schema(description = "变量参数列表")
    private List<Map<String, String>> paramsList;

}