package top.mddata.console.system.entity.base;

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
 * 字典项实体类。
 *
 * @param <E> 树节点
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DictItemBase<E extends TreeEntity<Long, E>> extends TreeEntity<Long, E> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_dict_item";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属字典
     */
    private Long dictId;

    /**
     * 父节点
     */
    private Long parentId;

    /**
     * 标识
     */
    private String uniqKey;

    /**
     * 名称
     */
    private String name;

    /**
     * 数据类型
     * [1-字符串 2-整型 3-布尔]
     */
    private String dataType;

    /**
     * 字典类型
     * [10-系统字典 20-枚举字典 30-业务字典]
     */
    private String dictType;

    /**
     * 树路径
     */
    private String treePath;

    /**
     * 状态
     */
    private Boolean state;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排序
     */
    private Integer weight;

    /**
     * 图标
     */
    private String icon;

    /**
     * 组件样式
     */
    private String cssStyle;

    /**
     * 组件类名
     */
    private String cssClass;

    /**
     * 组件属性
     * 用于Tag时，用于配置color属性
     * 用于Button时，用于配置type属性
     */
    private String propType;

    /**
     * 国际化配置
     */
    private String i18nJson;

}
