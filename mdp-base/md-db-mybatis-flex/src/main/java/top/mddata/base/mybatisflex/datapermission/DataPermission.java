package top.mddata.base.mybatisflex.datapermission;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解
 *
 * @author henhen
 * @since 2026年05月24日
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 表别名
     */
    String tableAlias() default "";

    /**
     * ID
     */
    String id() default "id";

    /**
     * 部门 ID
     */
    String deptId() default "dept_id";

    /**
     * 用户 ID
     */
    String userId() default "create_user";

    /**
     * 组织表名。本项目统一使用 mdc_org 组织表，
     * 通过 tree_path 前缀匹配表达
     * “本公司及以下 / 本部门及以下”的子树范围，
     * 不使用外键与 ancestors 字段
     */
    String deptTableAlias() default "mdc_org";
}
