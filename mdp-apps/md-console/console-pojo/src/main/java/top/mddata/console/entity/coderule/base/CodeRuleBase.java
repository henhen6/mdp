package top.mddata.console.entity.coderule.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 编码规则实体类。
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CodeRuleBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_code_rule";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规则编码（唯一）
     */
    private String code;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态
     */
    private Boolean state;

}
