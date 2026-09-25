package top.mddata.common.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.RelationOneToMany;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.common.entity.base.UserBase;
import top.mddata.common.entity.base.UserOrgRelBase;

import java.util.List;

/**
 * 用户实体类。
 * DO类：数据对象，可以在关联查询时，再次添加字段，重新生成代码时，忽略此文件。
 *
 * @author henhen6
 * @since 2025-11-12 15:48:54
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(UserBase.TABLE_NAME)
public class User extends UserBase {

    /**
     * 组织性质（注册身份载体，不持久化）
     * [1-总公司 90-开发者 99-运营]
     * 注册流程用于传递用户选择的身份
     */
    @Column(ignore = true)
    private Integer nature;

    /**
     * 用户拥有的部门
     */
    @RelationOneToMany(
            selfField = "id",
            targetField = "userId",
            targetTable = UserOrgRelBase.TABLE_NAME,
            valueField = "orgId"
    )
    @Column(ignore = true)
    private List<Long> orgIdList;
}
