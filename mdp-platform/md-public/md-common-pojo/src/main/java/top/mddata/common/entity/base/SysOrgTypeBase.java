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
 * 组织性质实体类。
 *
 * @author henhen6
 * @since 2025-10-19 09:48:19
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysOrgTypeBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "sys_org_type";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 组织id
     */
    private Long orgId;

    /**
     * 组织性质
     * [1-默认 90-开发者 99-超管]
     */
    private Integer orgType;

}
