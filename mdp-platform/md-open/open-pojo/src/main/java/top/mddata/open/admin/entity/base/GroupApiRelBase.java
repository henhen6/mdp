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
 * 分组拥有的对外接口实体类。
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
public class GroupApiRelBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_group_api_rel";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属分组
     * perm_group.id
     */
    private Long groupId;

    /**
     * 所属文档
     * api_info.id
     */
    private Long apiId;

}
