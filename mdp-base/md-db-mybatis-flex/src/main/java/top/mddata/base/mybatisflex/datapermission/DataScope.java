package top.mddata.base.mybatisflex.datapermission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 数据范围枚举。
 *
 * <p>存储于 mdc_role.data_scope；五档为系统内置实现，自定义实现（90）
 * 由开发人员编写 DataScopeCustomHandler 实现类，并在角色上配置其 Spring Bean 名
 * （mdc_role.data_scope_impl）。</p>
 *
 * @author henhen
 * @since 2026年05月24日
 */
@Getter
@AllArgsConstructor
public enum DataScope implements BaseEnum<String> {

    /**
     * 全部数据
     */
    ALL("10", "全部数据"),

    /**
     * 本公司及以下
     */
    COMPANY_AND_CHILD("20", "本公司及以下"),

    /**
     * 本部门及以下
     */
    DEPT_AND_CHILD("30", "本部门及以下"),

    /**
     * 本部门
     */
    DEPT("40", "本部门"),

    /**
     * 仅本人
     */
    SELF("50", "仅本人"),

    /**
     * 自定义实现（角色上配置 DataScopeCustomHandler 的 Bean 名）
     */
    CUSTOM("90", "自定义实现");

    /**
     * 档位编码（数据库存储用）
     */
    private final String code;

    /**
     * 档位描述
     */
    private final String desc;

    /**
     * 按编码取枚举（BaseEnum 无静态查找方法，各枚举自带）
     *
     * @param code 档位编码
     * @return 枚举，无匹配返回 null
     */
    public static DataScope getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (DataScope scope : values()) {
            if (scope.getCode().equals(code)) {
                return scope;
            }
        }
        return null;
    }
}
