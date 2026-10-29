package top.mddata.console.query.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;

/**
 * 编码规则 Query类（查询方法入参）。
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "编码规则")
public class CodeRuleQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规则编码
     */
    @Schema(description = "规则编码")
    private String code;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    private String name;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Boolean state;

}
