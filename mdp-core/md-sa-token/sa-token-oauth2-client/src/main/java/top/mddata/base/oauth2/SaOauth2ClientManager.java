package top.mddata.base.oauth2;

import top.mddata.base.oauth2.properties.Oauth2ClientConfig;

/**
 * Sa-Token-Oauth2 模块 总控类
 * @author henhen6
 * @since 2025/9/3 23:02
 */
public class SaOauth2ClientManager {
    /**
     * Oauth2 Client 端 配置 Bean
     */
    private static volatile Oauth2ClientConfig clientConfig;

    public static Oauth2ClientConfig getClientConfig() {
        if (clientConfig == null) {
            synchronized (SaOauth2ClientManager.class) {
                if (clientConfig == null) {
                    setClientConfig(new Oauth2ClientConfig());
                }
            }
        }
        return clientConfig;
    }

    public static void setClientConfig(Oauth2ClientConfig clientConfig) {
        SaOauth2ClientManager.clientConfig = clientConfig;
    }

}
