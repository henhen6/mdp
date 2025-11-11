package top.mddata.common.entity.base;

import top.mddata.base.base.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 组织用户关联实体类。
 *
 * @author henhen6
 * @since 2025-09-16 16:02:45
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysOrgUserRelBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "sys_org_user_rel";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 组织ID
     */
    private Long orgId;

    /**
     * 用户id
     */
    private Long userId;

}
