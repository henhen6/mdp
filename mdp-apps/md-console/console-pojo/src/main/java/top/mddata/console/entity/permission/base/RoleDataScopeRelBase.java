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
 * 角色数据范围授权实体类。
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoleDataScopeRelBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_role_data_scope_rel";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属角色
     */
    private Long roleId;

    /**
     * 所属菜单
     */
    private Long menuId;

    /**
     * 数据范围（编码见 DataScopeEnum：10/20/30/40/50/90）
     */
    private String dataScope;

    /**
     * 自定义实现的 Spring Bean 名（仅 dataScope=90 时非空）
     */
    private String dataScopeImpl;
}
