package top.mddata.base.oauth2.template;

import top.mddata.base.oauth2.processor.SaOauth2ClientProcessor;

/**
 * Oauth2 模板方法类 （Client端）
 * @author henhen6
 * @since 2025/9/3 20:54
 */
public class SaOauth2ClientUtil {

    /**
     * 构建URL：Server端 Oauth2登录授权地址，
     * <br/> 形如：{@code http://{host}:{port}/oauth2/authorize?response_type=code&client_id={client_id}&redirect_uri={redirect_uri}&scope={scope}&state={state}}
     * @param clientLoginUrl Client端登录地址
     * @param scope 权限范围
     * @param state 随机值
     * @return [Oauth2-Server端-认证地址 ]
     */
    public static String buildServerAuthorizeUrl(String clientLoginUrl, String scope, String state) {
        return SaOauth2ClientProcessor.getInstance().getOauth2ClientTemplate().buildServerAuthorizeUrl(clientLoginUrl, scope, state);
    }

}
