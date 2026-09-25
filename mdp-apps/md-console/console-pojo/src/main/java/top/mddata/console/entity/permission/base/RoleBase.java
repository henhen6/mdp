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
     * 组织性质
     * [1-总公司 90-开发者 99-运营]
     */
    private Integer orgNature;

    /**
     * 数据范围
     * [10-全部 20-本公司及以下 30-本部门及以下
     * 40-本部门 50-仅本人 90-自定义实现]
     */
    private String dataScope;

    /**
     * 自定义数据范围实现类（Spring Bean 名）
     * 仅 dataScope=90 时使用，实现 DataScopeCustomHandler 接口
     */
    private String dataScopeImpl;

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
