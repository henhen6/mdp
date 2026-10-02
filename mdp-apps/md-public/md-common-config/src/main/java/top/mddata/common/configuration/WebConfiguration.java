package top.mddata.common.configuration;

import io.undertow.Undertow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.mddata.base.boot.config.BaseConfig;
import top.mddata.common.apiperm.ApiPermSupport;
import top.mddata.common.configurer.HeaderThreadLocalConfigurer;
import top.mddata.common.configurer.TokenContextFilterConfigurer;
import top.mddata.common.properties.IgnoreProperties;
import top.mddata.common.properties.SystemProperties;
import top.mddata.common.undertow.UndertowServerFactoryCustomizer;

/**
 * 基础服务-Web配置
 *
 * @author henhen6
 * @since 2021-10-08
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({IgnoreProperties.class})
@RequiredArgsConstructor
public class WebConfiguration extends BaseConfig implements WebMvcConfigurer {
    private final IgnoreProperties ignoreProperties;
    private final ApiPermSupport apiPermSupport;

    @Bean
    @ConditionalOnClass(Undertow.class)
    public UndertowServerFactoryCustomizer getUndertowServerFactoryCustomizer() {
        return new UndertowServerFactoryCustomizer();
    }

    @Bean
    @ConditionalOnProperty(prefix = SystemProperties.PREFIX, name = "mode", havingValue = "cloud", matchIfMissing = true)
    public HeaderThreadLocalConfigurer getHeaderThreadLocalConfigurer() {
        return new HeaderThreadLocalConfigurer();
    }

    @Bean
    @ConditionalOnProperty(prefix = SystemProperties.PREFIX, name = "mode", havingValue = "boot")
    public TokenContextFilterConfigurer getTokenContextFilterConfigurer() {
        return new TokenContextFilterConfigurer(ignoreProperties, apiPermSupport);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

}
