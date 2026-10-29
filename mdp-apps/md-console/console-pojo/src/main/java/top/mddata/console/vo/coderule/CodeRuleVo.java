package top.mddata.console.vo.coderule;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import top.mddata.console.entity.coderule.base.CodeRuleBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 编码规则 VO类（通常用作Controller出参）。
 */
@Accessors(chain = true)
@Data
@Schema(description = "编码规则")
@Table(CodeRuleBase.TABLE_NAME)
public class CodeRuleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private Long id;

    /**
     * 规则编码（唯一）
     */
    @Schema(description = "规则编码")
    private String code;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    private String name;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

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

}
