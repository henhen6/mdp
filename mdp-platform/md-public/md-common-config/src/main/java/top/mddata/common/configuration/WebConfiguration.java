package top.mddata.common.configuration;

import top.mddata.common.configurer.HeaderThreadLocalConfigurer;
import top.mddata.common.configurer.TokenContextFilterConfigurer;
import top.mddata.common.properties.IgnoreProperties;
import top.mddata.common.properties.SystemProperties;
import top.mddata.common.undertow.UndertowServerFactoryCustomizer;
import top.mddata.base.boot.config.BaseConfig;
import top.mddata.base.constant.Constants;
import top.mddata.base.log.event.SysLogListener;
import io.undertow.Undertow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
        return new TokenContextFilterConfigurer(ignoreProperties);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    /**
     * 操作日志 监听器
     */
    @Bean
    @ConditionalOnExpression("${" + Constants.PROJECT_PREFIX + ".log.enabled:true} && 'DB'.equals('${" + Constants.PROJECT_PREFIX + ".log.type:LOGGER}')")
    public SysLogListener sysLogListener() {
        return new SysLogListener(data -> log.info("log: {}", data));
    }
}
