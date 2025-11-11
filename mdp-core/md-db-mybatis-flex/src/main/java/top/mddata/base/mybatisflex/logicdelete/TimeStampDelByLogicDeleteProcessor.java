package top.mddata.base.mybatisflex.logicdelete;

import top.mddata.base.utils.ContextUtil;
import cn.hutool.core.util.ArrayUtil;
import com.mybatisflex.core.dialect.IDialect;
import com.mybatisflex.core.logicdelete.impl.TimeStampLogicDeleteProcessor;
import com.mybatisflex.core.table.TableInfo;
import lombok.RequiredArgsConstructor;

import static com.mybatisflex.core.constant.SqlConsts.EQUALS;


/**
 * 时间戳类型的属性对应的逻辑删除处理器
 * 修改删除标志时，同时填充删除人 和 删除时间 字段
 * @since 2024年06月13日11:44:31
 * @author henhen6
 */
@RequiredArgsConstructor
public class TimeStampDelByLogicDeleteProcessor extends TimeStampLogicDeleteProcessor {
    private final String delByColumn;

    @Override
    public String buildLogicDeletedSet(String logicColumn, TableInfo tableInfo, IDialect dialect) {
        String sql = dialect.wrap(logicColumn) + EQUALS + getLogicDeletedValue();

        if (ContextUtil.getUserId() != null) {
            // 实体类中存在 delBy 字段，才记录删除人ID
            if (ArrayUtil.contains(tableInfo.getColumns(), delByColumn)) {
                sql += "," + dialect.wrap(delByColumn) + EQUALS + ContextUtil.getUserId();
            }
        }

        return sql;
    }

}
