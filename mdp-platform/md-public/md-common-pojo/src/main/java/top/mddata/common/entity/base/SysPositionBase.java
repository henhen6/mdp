package top.mddata.common.entity.base;

import top.mddata.base.base.entity.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 岗位实体类。
 *
 * @author henhen6
 * @since 2025-09-10 22:59:11
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysPositionBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "sys_position";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属组织
     * #sys_org
     * @Echo(api = EchoApi.ORG_ID_CLASS)
     */
    private Long orgId;

    /**
     * 名称
     */
    private String name;

    /**
     * 状态
     * 0-禁用 1-启用
     */
    private Boolean state;

    /**
     * 备注
     */
    private String remarks;

}
