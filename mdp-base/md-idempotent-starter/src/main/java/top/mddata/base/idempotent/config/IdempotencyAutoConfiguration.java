package top.mddata.base.idempotent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerInterceptor;
import top.mddata.base.cache.repository.CachePlusOps;
import top.mddata.base.constant.Constants;
import top.mddata.base.idempotent.advice.IdempotencyResponseAdvice;
import top.mddata.base.idempotent.controller.IdempotencyTokenController;
import top.mddata.base.idempotent.filter.IdempotencyRequestFilter;
import top.mddata.base.idempotent.interceptor.IdempotencyInterceptor;
import top.mddata.base.idempotent.properties.IdempotencyProperties;
import top.mddata.base.idempotent.repository.IdempotencyRepository;
import top.mddata.base.idempotent.repository.RedisIdempotencyRepository;
import top.mddata.base.idempotent.service.IdempotencyTokenService;
import top.mddata.base.idempotent.support.IdempotencyResponseWriter;

/**
 * 幂等组件自动配置，单体与微服务共用。
 * <p>
 * 令牌服务、令牌 Controller、响应 Advice 同时带有 Spring stereotype 注解，
 * 真实应用（启动类位于 top.mddata 包）会通过组件扫描装配它们，此处用
 * {@code @ConditionalOnMissingBean} 在扫描场景退避，保证上下文内各只有一个 Bean；
 * 未开启组件扫描的上下文（如第三方集成）则由本配置注册。
 * <p>
 * 对 {@link CachePlusOps} 不做 {@code @ConditionalOnBean} 判断：缓存依赖缺失必须启动失败，
 * 禁止静默关闭幂等保护。
 *
 * @author henhen6
 */
@AutoConfiguration
@EnableConfigurationProperties(IdempotencyProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({CachePlusOps.class, HandlerInterceptor.class})
@ConditionalOnProperty(prefix = IdempotencyProperties.PREFIX,
        name = Constants.ENABLED, havingValue = "true", matchIfMissing = true)
public class IdempotencyAutoConfiguration {

    /** Filter 注册顺序：在所有 Servlet Filter 之前完成请求包装，供拦截器读取缓存请求体。 */
    private static final int FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE + 30;

    @Bean
    @ConditionalOnMissingBean(IdempotencyRepository.class)
    public IdempotencyRepository idempotencyRepository(CachePlusOps cachePlusOps,
                                                       ObjectMapper objectMapper,
                                                       IdempotencyProperties properties) {
        return new RedisIdempotencyRepository(cachePlusOps, objectMapper, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyTokenService idempotencyTokenService(IdempotencyRepository idempotencyRepository) {
        return new IdempotencyTokenService(idempotencyRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyTokenController idempotencyTokenController(IdempotencyTokenService idempotencyTokenService) {
        return new IdempotencyTokenController(idempotencyTokenService);
    }

    @Bean
    public IdempotencyResponseWriter idempotencyResponseWriter(ObjectMapper objectMapper) {
        return new IdempotencyResponseWriter(objectMapper);
    }

    @Bean
    public IdempotencyInterceptor idempotencyInterceptor(IdempotencyRepository idempotencyRepository,
                                                         IdempotencyResponseWriter idempotencyResponseWriter) {
        return new IdempotencyInterceptor(idempotencyRepository, idempotencyResponseWriter);
    }

    @Bean
    public FilterRegistrationBean<IdempotencyRequestFilter> idempotencyRequestFilterRegistration(
            IdempotencyProperties properties, IdempotencyResponseWriter idempotencyResponseWriter) {
        FilterRegistrationBean<IdempotencyRequestFilter> registration =
                new FilterRegistrationBean<>(new IdempotencyRequestFilter(properties, idempotencyResponseWriter));
        // 仅 REQUEST 分发包装请求体，避免 FORWARD/ERROR 等内部分发被重复包装
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setOrder(FILTER_ORDER);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyResponseAdvice idempotencyResponseAdvice(IdempotencyRepository idempotencyRepository,
                                                               ObjectMapper objectMapper) {
        return new IdempotencyResponseAdvice(idempotencyRepository, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(IdempotencyWebMvcConfigurer.class)
    public IdempotencyWebMvcConfigurer idempotencyWebMvcConfigurer(IdempotencyInterceptor idempotencyInterceptor) {
        return new IdempotencyWebMvcConfigurer(idempotencyInterceptor);
    }
}
