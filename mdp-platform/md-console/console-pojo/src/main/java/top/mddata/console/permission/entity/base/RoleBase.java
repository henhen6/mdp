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
 * 角色实体类。
 *
 * @author henhen6
 * @since 2025-12-01 00:12:36
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoleBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_role";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色分类
     * [10-普通角色 20-管理员角色 30-权限集合]
     */
    private String roleCategory;

    /**
     * 角色类型
     * [10-功能角色 20-桌面角色 30-数据角色]
     */
    private String roleType;

    /**
     * 组织性质
     * [1-默认 90-开发者 99-运维]
     */
    private Integer orgNature;

    /**
     * 是否模版
     */
    private Boolean templateRole;

    /**
     * 说明
     */
    private String remarks;

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
