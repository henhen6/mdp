package top.mddata.console.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.powerjob.client.PowerJobClient;

/**
 * PowerJob OpenAPI 客户端装配。
 *
 * <p>配置了 powerjob.server.server-address 时才装配；未配置时定时消息发布会在
 * {@link top.mddata.console.service.message.impl.MsgTaskServiceImpl} 处得到明确的配置缺失报错。
 *
 * @author henhen6
 * @since 2026-10-31
 */
@Configuration
@EnableConfigurationProperties(PowerJobServerProperties.class)
public class PowerJobClientConfiguration {

    /**
     * 仅在配置了 powerjob.server.server-address 时装配（worker-server 等不发起 OpenAPI
     * 调用的节点无需配置，定时发送分支会用 ArgumentAssert 给出明确的配置缺失提示）。
     */
    @Bean
    @ConditionalOnProperty(prefix = "powerjob.server", name = "server-address")
    public PowerJobClient powerJobClient(PowerJobServerProperties properties) {
        return new PowerJobClient(properties.getServerAddress(), properties.getAppName(), properties.getPassword());
    }
}
