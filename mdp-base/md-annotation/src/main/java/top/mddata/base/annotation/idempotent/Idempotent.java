package top.mddata.base.annotation.idempotent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 声明同步 JSON 接口需要 Token 幂等保护。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 首次请求成功后的重复请求处理策略。
     *
     * @return 重复请求处理策略
     */
    DuplicateStrategy onDuplicate() default DuplicateStrategy.REPLAY;
}
