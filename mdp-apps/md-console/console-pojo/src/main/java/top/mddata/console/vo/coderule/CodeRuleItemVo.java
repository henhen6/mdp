package top.mddata.console.vo.coderule;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.Fastjson2TypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.entity.coderule.base.CodeRuleItemBase;

import java.io.Serial;
import java.io.Serializable;

/**
 * 编码规则明细 VO类（通常用作Controller出参）。
 */
@Accessors(chain = true)
@Data
@Schema(description = "编码规则明细")
@Table(CodeRuleItemBase.TABLE_NAME)
public class CodeRuleItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
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
    @Schema(description = "规则类别")
    private Integer ruleType;

    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer sort;

    /**
     * 类别差异化配置
     */
    @Schema(description = "类别差异化配置")
    @Column(typeHandler = Fastjson2TypeHandler.class)
    private CodeRuleItemConfig configJson;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Boolean state;

}
