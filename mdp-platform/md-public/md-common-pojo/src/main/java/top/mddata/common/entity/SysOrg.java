package top.mddata.common.entity;

import top.mddata.common.entity.base.SysOrgBase;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 组织实体类。
 * DO类：数据对象，可以在关联查询时，再次添加字段，重新生成代码时，忽略此文件。
 *
 * @author henhen6
 * @since 2025-09-10 22:58:51
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(SysOrgBase.TABLE_NAME)
public class SysOrg extends SysOrgBase<SysOrg> {
}
