package top.mddata.workbench.config;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 第三方登录配置。
 *
 * <p>密钥通过环境变量注入，支持 Nacos 动态刷新；启用某平台时必须配置完整，
 * 由平台工厂在启动阶段 fail fast。</p>
 *
 * @author tangyh
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "mdp.social")
public class SocialLoginProperties {

    /** 微信开放平台 */
    private ProviderConfig wechatOpen = new ProviderConfig();
    /** QQ */
    private ProviderConfig qq = new ProviderConfig();
    /** 钉钉 */
    private ProviderConfig dingTalk = new ProviderConfig();
    /** Gitee */
    private ProviderConfig gitee = new ProviderConfig();

    /**
     * 单个平台的连接配置
     */
    @Data
    public static class ProviderConfig {
        /** 是否启用 */
        private boolean enabled;
        /** 应用ID */
        private String clientId;
        /** 应用密钥 */
        private String clientSecret;
        /** 授权回调地址 */
        private String redirectUri;
        /** 钉钉企业 corpId，企业应用按需配置 */
        private String corpId;
        /** 是否申请跨应用 unionId */
        private boolean unionId;
        /** HTTP 请求超时时间（毫秒） */
        private int httpTimeoutMillis = 10_000;

        /**
         * 基础配置是否完整
         *
         * @return true 表示可以创建平台适配器
         */
        public boolean isComplete() {
            return StrUtil.isAllNotBlank(clientId, clientSecret, redirectUri);
        }
    }
}
