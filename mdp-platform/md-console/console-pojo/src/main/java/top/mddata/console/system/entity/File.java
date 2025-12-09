package top.mddata.console.system.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.console.system.entity.base.FileBase;

/**
 * 文件实体类。
 * DO类：数据对象，可以在关联查询时，再次添加字段，重新生成代码时，忽略此文件。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Table(FileBase.TABLE_NAME)
@FieldNameConstants
public class File extends FileBase {

}
