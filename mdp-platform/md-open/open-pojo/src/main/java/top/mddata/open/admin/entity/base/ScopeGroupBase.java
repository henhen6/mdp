package top.mddata.open.admin.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用权限分组实体类。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScopeGroupBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_scope_group";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 权重
     */
    private Long weight;

}
