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
 * 组织性质实体类。
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
public class OrgNatureBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_org_nature";

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
    private Integer nature;

}
