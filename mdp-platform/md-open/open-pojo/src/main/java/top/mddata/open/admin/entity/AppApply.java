package top.mddata.open.admin.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.open.admin.entity.base.AppApplyBase;

/**
 * 应用申请实体类。
 * DO类：数据对象，可以在关联查询时，再次添加字段，重新生成代码时，忽略此文件。
 *
 * @author henhen6
 * @since 2025-11-27 03:31:55
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Table(AppApplyBase.TABLE_NAME)
@FieldNameConstants
public class AppApply extends AppApplyBase {
}
