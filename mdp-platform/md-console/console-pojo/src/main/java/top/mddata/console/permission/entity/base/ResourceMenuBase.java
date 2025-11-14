package top.mddata.console.permission.entity.base;

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
 * 菜单实体类。
 *
 * @param <E> 树节点
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResourceMenuBase<E extends TreeEntity<Long, E>> extends TreeEntity<Long, E> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_resource_menu";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属应用
     */
    private Long appId;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     * [10-目录  20-菜单 30-内链 40-外链]
     */
    private String menuType;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 页面地址
     */
    private String component;

    /**
     * 重定向
     */
    private String redirect;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    private Boolean state;

    /**
     * 树路径
     */
    private String treePath;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 顺序号
     */
    private Integer weight;

    /**
     * 元数据
     *
     */
    private String meta;

    /**
     * 删除人
     */
    private Long deletedBy;

    /**
     * 删除标志
     */
    private Long deletedAt;

}
