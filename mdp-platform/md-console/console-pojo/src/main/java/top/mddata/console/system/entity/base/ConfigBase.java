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
 * 系统配置实体类。
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
public class ConfigBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_config";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参数分组
     */
    private String configGroup;

    /**
     * 参数标识
     */
    private String uniqKey;

    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数值
     */
    private String value;

    /**
     * 数据类型
     * [1-字符串 2-整型 3-布尔]
     */
    private String dataType;

    /**
     * 状态
     * [1-启用 0-禁用]
     */
    private Boolean state;

    /**
     * 备注
     */
    private String remark;

    /**
     * 当前机构id
     */
    private Long orgId;

    /**
     * 删除标识
     */
    private Long deletedAt;

    /**
     * 删除用户
     */
    private Long deletedBy;

}
