package top.mddata.console.dto.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 编码规则明细 DTO（写入方法入参）。
 */
@Accessors(chain = true)
@Data
@Schema(description = "编码规则明细")
public class CodeRuleItemDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID（新增时可空）
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属规则
     */
    @Schema(description = "所属规则")
    private Long ruleId;

    /**
     * 规则类别
     * [10-常量 20-日期 30-变量 40-流水号 50-随机码 60-校验位 70-GUID/雪花 80-组织编码 90-父单号截取]
     */
    @NotNull(message = "请选择规则类别")
    @Schema(description = "规则类别")
    private Integer ruleType;

    /**
     * 类别差异化配置
     */
    @NotNull(message = "请填写规则配置")
    @Schema(description = "类别差异化配置")
    private CodeRuleItemConfig configJson;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Boolean state;

}