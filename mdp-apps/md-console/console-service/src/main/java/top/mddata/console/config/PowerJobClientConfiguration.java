package top.mddata.console.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PowerJob OpenAPI 客户端装配。
 *
 * <p>Client 通过 {@link PowerJobClientHolder} 懒加载：启动时零网络请求，
 * 首次使用（定时消息发布）时才创建并鉴权，Server 未启动不影响应用启动。
 *
 * @author henhen6
 * @since 2026-10-31
 */
@Configuration
@EnableConfigurationProperties(PowerJobServerProperties.class)
public class PowerJobClientConfiguration {

    @Bean
    public PowerJobClientHolder powerJobClientHolder(PowerJobServerProperties properties) {
        return new PowerJobClientHolder(properties);
    }
}
