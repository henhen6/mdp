package top.mddata.base.oauth2.spring;

import cn.dev33.satoken.plugin.SaTokenPluginHolder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import top.mddata.base.oauth2.SaOauth2ClientManager;
import top.mddata.base.oauth2.processor.SaOauth2ClientProcessor;
import top.mddata.base.oauth2.properties.Oauth2ClientConfig;
import top.mddata.base.oauth2.template.SaOauth2ClientTemplate;

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

    /**
     * 主动触发 sa-token 插件加载（SPI 机制，读取 classpath 下 META-INF/satoken/ 目录声明的插件）。
     * <p>
     * oauth2-client 的 HTTP 请求依赖 SaHttpTemplate（如 sa-token-forest 插件注册的 Forest 实现），
     * 该模板默认是空壳实现（直接抛 NotImplException）。官方由 sa-token-spring-boot4-starter 的
     * SaBeanInject 触发插件加载，但客户端项目可能未引入该 starter（最小化引入场景），
     * 因此这里兜底触发一次。init 内部有 isLoader 防重入，与官方 starter 同时存在时不会重复加载。
     */
    @PostConstruct
    public void initSaTokenPlugins() {
        SaTokenPluginHolder.instance.init();
    }
}
