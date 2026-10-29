package top.mddata.console.dto.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 编码规则明细全量保存 DTO（整组提交，按列表顺序重排 sort，全删全插）。
 */
@Accessors(chain = true)
@Data
@Schema(description = "编码规则明细全量保存")
public class CodeRuleItemSaveDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    @NotNull(message = "请填写规则ID")
    @Schema(description = "规则ID")
    private Long ruleId;

    /**
     * 明细列表（顺序即拼接顺序）
     */
    @NotEmpty(message = "请填写规则明细")
    @Schema(description = "明细列表")
    private List<CodeRuleItemDto> items;

}