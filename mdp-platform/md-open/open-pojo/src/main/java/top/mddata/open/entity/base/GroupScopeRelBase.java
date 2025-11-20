package top.mddata.open.entity.base;

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
 * 分组拥有的oauth2权限实体类。
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
public class GroupScopeRelBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_group_scope_rel";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属分组
     */
    private Long groupId;

    /**
     * 所属权限
     */
    private Long scopeId;

}
