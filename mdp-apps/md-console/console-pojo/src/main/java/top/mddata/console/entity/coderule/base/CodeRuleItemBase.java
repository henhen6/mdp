package top.mddata.console.entity.coderule.base;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.core.handler.Fastjson2TypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;

import java.io.Serial;
import java.io.Serializable;

/**
 * 编码规则明细实体类。
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CodeRuleItemBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_code_rule_item";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属规则
     */
    private Long ruleId;

    /**
     * 规则类别
     * [10-常量 20-日期 30-变量 40-流水号 50-随机码 60-校验位 70-GUID/雪花 80-组织编码 90-父单号截取]
     */
    private Integer ruleType;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 状态
     */
    private Boolean state;

    /**
     * 类别差异化配置
     */
    @Column(typeHandler = Fastjson2TypeHandler.class)
    private CodeRuleItemConfig configJson;

}
