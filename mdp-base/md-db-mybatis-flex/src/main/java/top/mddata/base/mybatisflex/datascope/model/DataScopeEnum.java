package top.mddata.base.mybatisflex.datascope.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 数据范围枚举。
 *
 * <p>存储于 mdc_role_data_scope_rel.data_scope；五档为系统内置实现，
 * 自定义实现（90）由开发人员编写 DataScopeCustomHandler 实现类，
 * 并在授权记录上配置其 Spring Bean 名（data_scope_impl）。</p>
 *
 * @author henhen
 * @since 2026年09月26日
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum implements BaseEnum<String> {

    /** 全部数据 */
    ALL("10", "全部数据", 60),
    /** 本公司及以下 */
    COMPANY_AND_CHILD("20", "本公司及以下", 40),
    /** 本部门及以下 */
    DEPT_AND_CHILD("30", "本部门及以下", 30),
    /** 本部门 */
    DEPT("40", "本部门", 20),
    /** 仅本人 */
    SELF("50", "仅本人", 10),
    /** 自定义实现（授权记录上配置 DataScopeCustomHandler 的 Bean 名） */
    CUSTOM("90", "自定义实现", 50);

    /** 档位编码（数据库存储用） */
    private final String code;
    /** 档位描述 */
    private final String desc;
    /**
     * 并集合并优先级：全部 > 自定义 > 公司及以下 > 部门及以下 > 部门 > 仅本人。
     * 自定义视为"比组织档更大"，因为其实现范围不受内置档约束。
     * 不生成 getter：统一走 priority() 方法，避免双访问器并存
     */
    @Getter(AccessLevel.NONE)
    private final int priority;

    /**
     * 优先级数值（用于合并比较；不用 getter 命名是为了调用更贴近语义）
     */
    public int priority() {
        return priority;
    }

    /**
     * 按编码取枚举
     *
     * @param code 档位编码
     * @return 枚举，无匹配返回 null
     */
    public static DataScopeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (DataScopeEnum scope : values()) {
            if (scope.getCode().equals(code)) {
                return scope;
            }
        }
        return null;
    }
}
