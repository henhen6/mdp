package top.mddata.common.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.TreeEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 组织实体类。
 *
 * @param <E> 树节点
 * @author henhen6
 * @since 2025-11-12 15:49:10
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrgBase<E extends TreeEntity<Long, E>> extends TreeEntity<Long, E> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_org";

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
     * 组织性质
     * [1-总公司 90-开发者 99-运营]
     * 一个组织有且只有一个性质；部门冗余同步所属公司的性质，读取时以顶级公司为准
     */
    private Integer nature;

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
