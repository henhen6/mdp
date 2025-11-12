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
 * 按钮实体类。
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
public class ResourceButtonBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_resource_button";

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
     * 图标
     */
    private String icon;

    /**
     * 显示方式
     *  [01-文字 02-图标 03-文字和图标]
     */
    private String showMode;

    /**
     * 按钮类型 
     * [01-default 02-primary 03-info 04-success 05-warning 06-error]
     */
    private String btnType;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    private Boolean state;

    /**
     * 删除人
     */
    private Long deletedBy;

    /**
     * 删除标志
     */
    private Long deletedAt;

}
