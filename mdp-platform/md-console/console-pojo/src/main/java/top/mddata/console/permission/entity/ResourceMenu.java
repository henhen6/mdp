package top.mddata.console.permission.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.console.permission.entity.base.ResourceMenuBase;

/**
 * 菜单实体类。
 * DO类：数据对象，可以在关联查询时，再次添加字段，重新生成代码时，忽略此文件。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(ResourceMenuBase.TABLE_NAME)
public class ResourceMenu extends ResourceMenuBase<ResourceMenu> {
}
