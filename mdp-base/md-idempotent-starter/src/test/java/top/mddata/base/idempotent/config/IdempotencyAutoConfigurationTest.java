package top.mddata.base.idempotent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import top.mddata.base.cache.repository.CachePlusOps;
import top.mddata.base.idempotent.advice.IdempotencyResponseAdvice;
import top.mddata.base.idempotent.controller.IdempotencyTokenController;
import top.mddata.base.idempotent.filter.IdempotencyRequestFilter;
import top.mddata.base.idempotent.interceptor.IdempotencyInterceptor;
import top.mddata.base.idempotent.repository.IdempotencyRepository;
import top.mddata.base.idempotent.service.IdempotencyTokenService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * {@link IdempotencyAutoConfiguration} 自动装配行为测试。
 * <p>
 * 组件扫描去重测试模拟单体/微服务应用扫描 top.mddata 包的场景：
 * 令牌服务、令牌 Controller、响应 Advice 既有 @Service/@RestController/@ControllerAdvice 注解会被扫描装配，
 * 自动配置必须退避，保证上下文内各只有一个 Bean。
 *
 * @author henhen6
 */
class IdempotencyAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(IdempotencyAutoConfiguration.class))
            .withBean(CachePlusOps.class, () -> mock(CachePlusOps.class))
            .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    void registersSharedMvcComponents() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(IdempotencyRepository.class);
            assertThat(context).hasSingleBean(IdempotencyTokenService.class);
            assertThat(context).hasSingleBean(IdempotencyTokenController.class);
            assertThat(context).hasSingleBean(IdempotencyInterceptor.class);
            assertThat(context).hasSingleBean(IdempotencyResponseAdvice.class);
            assertThat(context).hasSingleBean(IdempotencyWebMvcConfigurer.class);
        });
    }

    @Test
    void filterRegisteredForRequestDispatchWithFixedOrder() {
        runner.run(context -> {
            FilterRegistrationBean<?> registration = context.getBean(FilterRegistrationBean.class);
            assertThat(registration.getFilter()).isInstanceOf(IdempotencyRequestFilter.class);
            // 仅 REQUEST 分发包装请求体，且先于 DispatcherServlet 与拦截器完成包装
            assertThat(registration.determineDispatcherTypes()).containsExactly(DispatcherType.REQUEST);
            assertThat(registration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 30);
            assertThat(registration.getUrlPatterns()).containsExactly("/*");
        });
    }

    @Test
    void interceptorRegisteredWithHighestMvcOrder() {
        CapturingRegistry registry = new CapturingRegistry();
        IdempotencyWebMvcConfigurer configurer =
                new IdempotencyWebMvcConfigurer(mock(IdempotencyInterceptor.class));
        configurer.addInterceptors(registry);
        InterceptorRegistration registration = registry.captured();
        assertThat(registration).isNotNull();
        // InterceptorRegistry 不暴露 registration 列表，order 与路径模式通过反射读取
        assertThat(ReflectionTestUtils.getField(registration, "order")).isEqualTo(-10);
        assertThat(ReflectionTestUtils.getField(registration, "includePatterns")).isEqualTo(List.of("/**"));
    }

    @Test
    void disabledPropertyRemovesAllComponents() {
        runner.withPropertyValues("mdp.idempotent.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(IdempotencyRepository.class));
    }

    @Test
    void missingCacheFailsInsteadOfDisablingProtection() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(IdempotencyAutoConfiguration.class))
                .withBean(ObjectMapper.class, ObjectMapper::new)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void componentScanDoesNotDuplicateStarterBeans() {
        runner.withUserConfiguration(ScanConfiguration.class).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(IdempotencyTokenService.class);
            assertThat(context).hasSingleBean(IdempotencyTokenController.class);
            assertThat(context).hasSingleBean(IdempotencyResponseAdvice.class);
            assertThat(context).hasSingleBean(IdempotencyRepository.class);
            assertThat(context).hasSingleBean(IdempotencyWebMvcConfigurer.class);
        });
    }

    @Test
    void componentScanWithDisabledPropertySkipsScannedComponents() {
        // 开关必须同时约束组件扫描装配的三个 stereotype Bean，否则禁用后扫描 Bean 因仓储缺失启动失败
        runner.withUserConfiguration(ScanConfiguration.class)
                .withPropertyValues("mdp.idempotent.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(IdempotencyTokenService.class);
                    assertThat(context).doesNotHaveBean(IdempotencyTokenController.class);
                    assertThat(context).doesNotHaveBean(IdempotencyResponseAdvice.class);
                    assertThat(context).doesNotHaveBean(IdempotencyRepository.class);
                });
    }

    /**
     * 模拟真实应用组件扫描：仅扫描包含 Spring stereotype 注解的三个幂等包。
     */
    @Configuration
    @ComponentScan(basePackageClasses = {IdempotencyTokenService.class,
            IdempotencyTokenController.class, IdempotencyResponseAdvice.class})
    static class ScanConfiguration {
    }

    /**
     * 捕获注册结果，绕开 InterceptorRegistry#getInterceptors 的 protected 可见性。
     */
    private static final class CapturingRegistry extends InterceptorRegistry {
        private InterceptorRegistration registration;

        @Override
        public InterceptorRegistration addInterceptor(HandlerInterceptor interceptor) {
            this.registration = super.addInterceptor(interceptor);
            return this.registration;
        }

        private InterceptorRegistration captured() {
            return registration;
        }
    }
}
