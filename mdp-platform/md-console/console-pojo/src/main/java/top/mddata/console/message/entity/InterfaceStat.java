package top.mddata.console.message.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.console.message.entity.base.InterfaceStatBase;

/**
 * 接口统计实体类。
 * DO类：数据对象，可以在关联查询时，再次添加字段，重新生成代码时，忽略此文件。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@FieldNameConstants
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(InterfaceStatBase.TABLE_NAME)
public class InterfaceStat extends InterfaceStatBase {
}
