package top.mddata.base.sso.spring;

import cn.dev33.satoken.plugin.SaTokenPluginHolder;
import cn.dev33.satoken.sso.SaSsoClientManager;
import cn.dev33.satoken.sso.config.SaSsoClientConfig;
import cn.dev33.satoken.sso.processor.SaSsoClientProcessor;
import cn.dev33.satoken.sso.template.SaSsoClientTemplate;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.util.Map;


/**
 * 注入 Sa-Token SSO 客户端 所需要的 Bean
 * @author henhen
 * @since 2026/1/10 18:12
 */
@ConditionalOnClass(SaSsoClientManager.class)
public class SaSsoClientBeanInject {

    // 1. 注入默认单客户端配置（显式指定 Bean 名，避免歧义）
    @Autowired(required = false)
    @Qualifier("saSsoClientConfig")
    private SaSsoClientConfig defaultSaSsoClientConfig;

    // 2. 注入配置绑定的多客户端 Map（关键：@Qualifier 指定命名的 Map Bean）
    @Autowired(required = false)
    @Qualifier("ssoClientsConfigMap") // 明确注入配置绑定的 Map，而非 Bean 收集的 Map
    private Map<String, SaSsoClientConfig> ssoClientsConfigMap;


    /**
     * Bean初始化完成后统一设置配置（执行顺序完全可控）
     */
    @PostConstruct
    public void initSsoClientConfig() {
        // 第一步：设置默认单客户端配置
        if (defaultSaSsoClientConfig != null) {
            SaSsoClientManager.setClientConfig(defaultSaSsoClientConfig);
        }

        // 第二步：设置多客户端配置Map（此时Map已完全初始化）
        if (ssoClientsConfigMap != null && !ssoClientsConfigMap.isEmpty()) {
            SaSsoClientManager.setClientConfigMap(ssoClientsConfigMap);
        }

        // 第三步：兜底触发 sa-token 插件加载（SPI 机制，读取 classpath 下 META-INF/satoken/ 目录声明的插件）。
        // SSO 模式三的消息推送依赖 SaHttpTemplate（如 sa-token-forest 插件注册的 Forest 实现），该模板默认是
        // 空壳实现（直接抛 NotImplException）。官方由 sa-token-spring-boot4-starter 的 SaBeanInject 触发插件
        // 加载，但客户端项目可能未引入该 starter（最小化引入场景），因此这里兜底触发一次。
        // init 内部有 isLoader 防重入，与官方 starter 同时存在时不会重复加载。
        SaTokenPluginHolder.instance.init();
    }

    /**
     * 注入 SSO 模板代码类 (Client 端)
     *
     * @param ssoClientTemplate /
     */
    @Autowired(required = false)
    public void setSaSsoClientTemplate(SaSsoClientTemplate ssoClientTemplate) {
        SaSsoClientProcessor.getInstance().setSsoClientTemplate(ssoClientTemplate);
    }
}
