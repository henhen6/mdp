package com.gitee.sop.support.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 六如
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Open {

    /**
     * 接口名，如：member.user.get
     */
    String value();

    /**
     * 版本号，默认版本号是"1.0"
     */
    String version() default "1.0";

    /**
     * 指定接口是否需要授权才能访问，可在admin中进行授权
     */
    boolean permission() default false;

    /**
     * 是否需要accessToken，设置为true时，网关会校验accessToken非空，
     * 并由TokenValidateInterceptor在拦截器阶段校验其真实有效性（Redis中{appKey}:{token}映射）
     */
    boolean needToken() default true;

    /**
     * 是否需要签名校验，设置为true，网关端会校验请求签名（使用RSA2算法）
     */
    boolean needSign() default false;

    /**
     * 是否有公共响应参数,默认true
     * <pre>
     * 如果设置true,返回结果如下
     * {
     *     "code": "0",
     *     "msg": "",
     *     "subCode": "",
     *     "subMsg": "",
     *     "data": {
     *         "id": 1,
     *         "name": "Jim"
     *     }
     * }
     * 如果设置false,只返回data部分:
     * {
     *  "id": 1,
     *  "name": "Jim"
     * }
     * </pre>
     */
    boolean hasCommonResponse() default true;

}
