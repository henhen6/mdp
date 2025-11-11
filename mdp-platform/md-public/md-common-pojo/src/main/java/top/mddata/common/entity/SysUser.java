package top.mddata.common.entity;

import top.mddata.common.entity.base.SysOrgUserRelBase;
import top.mddata.common.entity.base.SysUserBase;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.RelationOneToMany;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 用户实体类。
 * DO类：数据对象，可以在关联查询时，再次添加字段，重新生成代码时，忽略此文件。
 *
 * @author henhen6
 * @since 2025-10-19 09:45:12
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(SysUserBase.TABLE_NAME)
public class SysUser extends SysUserBase {

    /**
     * 用户拥有的部门
     */
    @RelationOneToMany(
            selfField = "id",
            targetField = "userId",
            targetTable = SysOrgUserRelBase.TABLE_NAME,
            valueField = "orgId"
    )
    @Column(ignore = true)
    private List<Long> orgIdList;
}
