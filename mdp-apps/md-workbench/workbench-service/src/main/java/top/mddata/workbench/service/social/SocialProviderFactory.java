package top.mddata.workbench.service.social;

import org.springframework.stereotype.Component;
import top.mddata.base.exception.BizException;
import top.mddata.workbench.config.SocialLoginProperties;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.social.impl.JustAuthSocialProvider;

import java.util.Arrays;
import java.util.List;

/**
 * 第三方登录平台工厂。
 *
 * <p>启动时校验所有已启用平台的配置完整性；每次获取都创建新的适配器，
 * 避免 JustAuth 请求对象共享授权状态。</p>
 *
 * @author tangyh
 */
@Component
public class SocialProviderFactory {

    private final SocialLoginProperties properties;
    private final JustAuthRequestFactory requestFactory;

    public SocialProviderFactory(SocialLoginProperties properties,
                                 JustAuthRequestFactory requestFactory) {
        this.properties = properties;
        this.requestFactory = requestFactory;
        validate();
    }

    /**
     * 获取指定平台的适配器
     *
     * @param provider 第三方平台
     * @return 全新的平台适配器
     */
    public SocialProvider get(SocialProviderEnum provider) {
        SocialLoginProperties.ProviderConfig config = configOf(provider);
        if (!config.isEnabled()) {
            throw new BizException("第三方登录平台[" + provider.getLabel() + "]未启用");
        }
        return new JustAuthSocialProvider(provider, config, requestFactory.create(provider, config));
    }

    /**
     * 列出所有启用且配置完整的平台
     *
     * @return 已启用平台集合
     */
    public List<SocialProviderEnum> enabledProviders() {
        return Arrays.stream(SocialProviderEnum.values())
                .filter(provider -> {
                    SocialLoginProperties.ProviderConfig config = configOf(provider);
                    return config.isEnabled() && config.isComplete();
                })
                .toList();
    }

    private void validate() {
        Arrays.stream(SocialProviderEnum.values())
                .filter(provider -> {
                    SocialLoginProperties.ProviderConfig config = configOf(provider);
                    return config.isEnabled() && !config.isComplete();
                })
                .findFirst()
                .ifPresent(provider -> {
                    throw new BizException("第三方登录平台[" + provider.getLabel()
                            + "]已启用但配置不完整");
                });
    }

    private SocialLoginProperties.ProviderConfig configOf(SocialProviderEnum provider) {
        return switch (provider) {
            case WECHAT_OPEN -> properties.getWechatOpen();
            case QQ -> properties.getQq();
            case DINGTALK -> properties.getDingTalk();
        };
    }
}
