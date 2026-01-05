package top.mddata.gateway.sop.pojo.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.gateway.sop.pojo.entity.base.GroupApiRelBase;

/**
 * 分组拥有的对外接口实体类。
 * DO类：数据对象，可以在关联查询时，再次添加字段，重新生成代码时，忽略此文件。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(GroupApiRelBase.TABLE_NAME)
public class GroupApiRel extends GroupApiRelBase {
}
