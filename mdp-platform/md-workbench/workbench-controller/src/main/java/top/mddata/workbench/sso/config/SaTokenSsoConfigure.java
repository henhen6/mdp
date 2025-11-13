package top.mddata.workbench.sso.config;

import cn.dev33.satoken.sso.config.SaSsoServerConfig;
import cn.dev33.satoken.sso.exception.SaSsoException;
import cn.dev33.satoken.sso.processor.SaSsoServerProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token SSO 模块相关配置
 * @author henhen6
 * @date 2025年07月14日00:37:59
 */
@Configuration
public class SaTokenSsoConfigure {

    @Autowired
    private CustomSaSsoServerTemplate customSaSsoServerTemplate;

    @Autowired
    private void configSso(SaSsoServerConfig saSsoServerConfig) {
        // 增强 SaSsoServerTemplate 对象
        SaSsoServerProcessor.instance.ssoServerTemplate = customSaSsoServerTemplate;

        SaSsoServerProcessor.instance.ssoServerTemplate.strategy.notLoginView = () -> {
            throw new SaSsoException("暂无此功能");
        };

    }

}
