package top.mddata.open.admin.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用拥有的权限分组实体类。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppGroupRelBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_app_group_rel";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属应用
     */
    private Long appId;

    /**
     * 分组
     * perm_group.id
     */
    private Long groupId;

}
