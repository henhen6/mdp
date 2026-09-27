package top.mddata.console.entity.permission;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.console.entity.permission.base.ResourceApiBase;

/**
 * 接口权限 实体。
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(ResourceApiBase.TABLE_NAME)
public class ResourceApi extends ResourceApiBase {
}
