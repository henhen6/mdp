package top.mddata.common.entity.base;

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
 * 用户角色关联实体类。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserRoleRelBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_user_role_rel";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属角色
     */
    private Long roleId;

    /**
     * 所属用户
     */
    private Long userId;

}
