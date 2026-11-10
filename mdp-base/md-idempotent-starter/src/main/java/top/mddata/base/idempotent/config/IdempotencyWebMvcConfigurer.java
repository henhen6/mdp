package top.mddata.base.idempotent.config;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.mddata.base.idempotent.interceptor.IdempotencyInterceptor;

/**
 * 幂等 MVC 配置：把幂等拦截器注册到拦截器链最前，保证幂等抢占先于业务拦截器执行。
 *
 * @author henhen6
 */
public class IdempotencyWebMvcConfigurer implements WebMvcConfigurer {

    /** 拦截器 order：负值优先于默认的拦截器注册，抢占必须发生在业务拦截器之前。 */
    private static final int INTERCEPTOR_ORDER = -10;

    private final IdempotencyInterceptor idempotencyInterceptor;

    /**
     * @param idempotencyInterceptor 幂等 MVC 拦截器
     */
    public IdempotencyWebMvcConfigurer(IdempotencyInterceptor idempotencyInterceptor) {
        this.idempotencyInterceptor = idempotencyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(idempotencyInterceptor).addPathPatterns("/**").order(INTERCEPTOR_ORDER);
    }
}
