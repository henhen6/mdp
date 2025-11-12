package top.mddata.console.permission.entity.base;

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
 * 数据权限实体类。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResourceDataPermBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_resource_data_perm";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属菜单
     */
    private Long menuId;

    /**
     * 编码
     * 唯一编码，用于区分资源
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    private Boolean state;

    /**
     * 默认权限
     */
    private Boolean isDef;

    /**
     * 数据范围
     * [01-全部 02-本单位及子级 03-本单位 04-本部门及子级 05-本部门 06-个人 07-自定义]
     */
    private String dataScope;

    /**
     * 实现类
     */
    private String customClass;

    /**
     * 顺序号
     */
    private Integer weight;

    /**
     * 删除人
     */
    private Long deletedBy;

    /**
     * 删除标志
     */
    private Long deletedAt;

}
