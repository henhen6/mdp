package top.mddata.common.entity;

import top.mddata.common.entity.base.SysOrgUserRelBase;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 组织用户关联实体类。
 * DO类：数据对象，可以在关联查询时，再次添加字段，重新生成代码时，忽略此文件。
 *
 * @author henhen6
 * @since 2025-09-16 16:02:45
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(SysOrgUserRelBase.TABLE_NAME)
public class SysOrgUserRel extends SysOrgUserRelBase {
}
