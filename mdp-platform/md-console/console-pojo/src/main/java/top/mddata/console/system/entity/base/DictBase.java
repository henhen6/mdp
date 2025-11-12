package top.mddata.console.system.entity.base;

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
 * 字典实体类。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DictBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_dict";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典项结构
     * [01-列表 02-树结构]
     */
    private String itemType;

    /**
     * 字典分组
     */
    private String dictGroup;

    /**
     * 字典类型
     * [10-系统字典 20-枚举字典 30-业务字典]
     */
    private String dictType;

    /**
     * 数据类型
     * [1-字符串 2-整型 3-布尔]
     */
    private String dataType;

    /**
     * 标识
     */
    private String uniqKey;

    /**
     * 名称
     */
    private String name;

    /**
     * 状态
     */
    private Boolean state;

    /**
     * 备注
     */
    private String remark;

}
