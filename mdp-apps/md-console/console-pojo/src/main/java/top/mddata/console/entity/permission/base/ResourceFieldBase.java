package top.mddata.console.entity.permission.base;

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
 * 字段权限实体类。
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
public class ResourceFieldBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_resource_field";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属菜单
     */
    private Long menuId;

    /**
     * 实体类字段
     */
    private String property;

    /**
     * 名称
     */
    private String name;

    /**
     * 处理动作
     * [10-隐藏 20-脱敏]
     */
    private Integer ruleType;

    /**
     * 脱敏规则(rule_type=20时必填)，取 MaskManager 注册名
     */
    private String maskRule;

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
