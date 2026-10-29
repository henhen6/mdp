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
 * 编码流水号实体类。
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CodeSnBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_code_sn";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规则明细
     */
    private Long ruleItemId;

    /**
     * 周期标识（归零规则对应的周期 key，永不归零时为固定值）
     */
    private String periodKey;

    /**
     * 当前流水值
     */
    private Long currentValue;

}
