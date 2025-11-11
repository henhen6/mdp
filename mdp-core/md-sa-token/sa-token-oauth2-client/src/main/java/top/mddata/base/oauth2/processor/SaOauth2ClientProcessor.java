package top.mddata.base.oauth2.processor;

import top.mddata.base.oauth2.template.SaOauth2ClientTemplate;
import lombok.Getter;
import lombok.Setter;

/**
 * Oauth2 请求处理器 （Client端）
 * @author henhen6
 * @since 2025/9/3 20:56
 */
@Getter
@Setter
public class SaOauth2ClientProcessor {
    @Getter
    private static SaOauth2ClientProcessor instance = new SaOauth2ClientProcessor();

    private SaOauth2ClientTemplate oauth2ClientTemplate = new SaOauth2ClientTemplate();


    public static void setInstance(SaOauth2ClientProcessor instance) {
        SaOauth2ClientProcessor.instance = instance;
    }
}
