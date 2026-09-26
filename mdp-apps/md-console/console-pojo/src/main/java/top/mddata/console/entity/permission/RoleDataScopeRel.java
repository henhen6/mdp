package top.mddata.console.entity.permission;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.console.entity.permission.base.RoleDataScopeRelBase;

/**
 * 角色数据范围授权实体类。
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(RoleDataScopeRelBase.TABLE_NAME)
public class RoleDataScopeRel extends RoleDataScopeRelBase {
}
