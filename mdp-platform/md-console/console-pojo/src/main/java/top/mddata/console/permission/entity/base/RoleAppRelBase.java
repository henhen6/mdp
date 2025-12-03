package top.mddata.console.permission.entity.base;

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
 * 角色应用关联实体类。
 *
 * @author henhen6
 * @since 2025-12-03 14:54:25
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoleAppRelBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_role_app_rel";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属角色
     */
    private Long roleId;

    /**
     * 所属应用
     */
    private Long appId;

}
