package top.mddata.console.entity.coderule;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.console.entity.coderule.base.CodeSnBase;

import java.io.Serial;

/**
 * 编码流水号实体类。
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Table(CodeSnBase.TABLE_NAME)
public class CodeSn extends CodeSnBase {
    @Serial
    private static final long serialVersionUID = 1L;
}
