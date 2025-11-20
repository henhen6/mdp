package top.mddata.base.oauth2.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import top.mddata.base.oauth2.SaOauth2ClientManager;
import top.mddata.base.oauth2.processor.SaOauth2ClientProcessor;
import top.mddata.base.oauth2.properties.Oauth2ClientConfig;
import top.mddata.base.oauth2.template.SaOauth2ClientTemplate;

/**
 * 注册 Sa-Token oauth2 client 所需要的 Bean
 * @author henhen6
 * @since 2025/9/4 12:48
 */
@ConditionalOnClass(SaOauth2ClientManager.class)
public class SaOauth2ClientBeanRegister {
    /**
     * 获取 Oauth2 Server 端 配置对象
     * @return 配置对象
     */
    @ConfigurationProperties(prefix = "sa-token.oauth2-client")
    @Bean
    public Oauth2ClientConfig getOauth2ClientConfig() {
        return new Oauth2ClientConfig();
    }

    /**
     * 获取 Oauth2 Client 端 oauth2ClientTemplate
     *
     * @return /
     */
    @Bean
    @ConditionalOnMissingBean(SaOauth2ClientTemplate.class)
    public SaOauth2ClientTemplate getSaSsoClientTemplate() {
        return SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate();
    }

}
