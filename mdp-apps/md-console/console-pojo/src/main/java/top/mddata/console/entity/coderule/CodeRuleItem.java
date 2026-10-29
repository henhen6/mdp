package top.mddata.console.entity.coderule;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.console.entity.coderule.base.CodeRuleItemBase;

import java.io.Serial;

/**
 * 编码规则明细实体类。
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Table(CodeRuleItemBase.TABLE_NAME)
public class CodeRuleItem extends CodeRuleItemBase {
    @Serial
    private static final long serialVersionUID = 1L;
}
