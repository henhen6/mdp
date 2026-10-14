package top.mddata.base.boot.inner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 内部端点保护自动装配：为所有引入 md-boot 的服务统一注册 /inner/** 令牌拦截器。
 * 单体（md-all-boot）与微服务（md-all-cloud 包含 md-all-boot）均覆盖；
 * 仅 Servlet Web 应用激活，非 Web 应用（如纯任务节点）不注册。
 *
 * @author henhen
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class InnerEndpointAutoConfiguration {

    @Value("${mdp.inner.token:}")
    private String innerToken;

    @Bean
    public WebMvcConfigurer innerAuthWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new InnerAuthInterceptor(innerToken)).addPathPatterns("/inner/**");
            }
        };
    }
}
