package top.mddata.base.oauth2.spring;

import top.mddata.base.oauth2.SaOauth2ClientManager;
import top.mddata.base.oauth2.processor.SaOauth2ClientProcessor;
import top.mddata.base.oauth2.properties.Oauth2ClientConfig;
import top.mddata.base.oauth2.template.SaOauth2ClientTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * 注入 Sa-Token Oauth2 client 所需要的 Bean
 * @author henhen6
 * @since 2025/9/4 12:48
 */
@ConditionalOnClass(SaOauth2ClientManager.class)
public class SaOauth2ClientBeanInject {


    /**
     * 注入 Sa-Token SSO Server 端 配置类
     *
     * @param clientConfig 配置对象
     */
    @Autowired(required = false)
    public void setSaSsoServerConfig(Oauth2ClientConfig clientConfig) {
        SaOauth2ClientManager.setClientConfig(clientConfig);
    }


    /**
     * 注入 SSO 模板代码类 (Client 端)
     *
     * @param oauth2ClientTemplate /
     */
    @Autowired(required = false)
    public void setSaSsoClientTemplate(SaOauth2ClientTemplate oauth2ClientTemplate) {
        SaOauth2ClientProcessor.getInstance().setOauth2ClientTemplate(oauth2ClientTemplate);
    }
}
