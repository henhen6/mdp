package top.mddata.common.entity.base;

import top.mddata.base.base.entity.TreeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 组织实体类。
 *
 * @param <E> 子类
 * @author henhen6
 * @since 2025-09-10 22:58:51
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysOrgBase<E extends TreeEntity<Long, E>> extends TreeEntity<Long, E> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "sys_org";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     * [10-单位 20-部门]
     */
    private String orgType;

    /**
     * 简称
     */
    private String shortName;

    /**
     * 树路径
     */
    private String treePath;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    private Boolean state;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 删除人
     */
    private Long deletedBy;

    /**
     * 删除标识
     */
    private Long deletedAt;

}
