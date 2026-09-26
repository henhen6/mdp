package top.mddata.base.mybatisflex.datascope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限埋点注解。
 *
 * <p>标注在 Service 或 Mapper 的查询方法上，方法执行期间的所有 SELECT
 * 由 DataScopeInterceptor 按（用户角色 × 菜单）授权注入数据范围条件。</p>
 *
 * @author henhen
 * @since 2026年09月26日
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 菜单 code（mdc_resource_menu.code，全局唯一业务键，跨环境稳定）
     */
    String code();

    /**
     * 组织列名（组织类档位的过滤列；不声明则该查询不支持组织类档位）
     */
    String orgColumn() default "";

    /**
     * 用户列名（仅本人档的过滤列）
     */
    String userColumn() default "created_by";

    /**
     * 过滤目标表别名，支持 1..n 个；仅改写声明别名涉及的表。
     * 单表查询可省略（省略时仅允许单表语句，多表语句 fail fast）
     */
    String[] tableAliases() default {};
}
