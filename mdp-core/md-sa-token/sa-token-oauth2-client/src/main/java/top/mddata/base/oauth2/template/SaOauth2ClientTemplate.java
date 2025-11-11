package top.mddata.base.oauth2.template;

import top.mddata.base.oauth2.SaOauth2ClientManager;
import top.mddata.base.oauth2.name.ParamName;
import top.mddata.base.oauth2.properties.Oauth2ClientConfig;
import cn.dev33.satoken.util.SaFoxUtil;
import lombok.Getter;

/**
 * Oauth2 模板方法类 （Client端）
 * @author henhen6
 * @since 2025/9/3 20:58
 */
@Getter
public class SaOauth2ClientTemplate {
    /**
     * 所有参数名称
     */
    private ParamName paramName = new ParamName();

    public SaOauth2ClientTemplate setParamName(ParamName paramName) {
        this.paramName = paramName;
        return this;
    }

    /**
     构建URL：Server端 Oauth2登录授权地址，
     * <br/> 形如：{@code http://{host}:{port}/oauth2/authorize?response_type=code&client_id={client_id}&redirect_uri={redirect_uri}&scope={scope}&state={state}}
     * @param clientLoginUrl Client端登录地址
     * @param scope 权限范围
     * @param state 随机值
     * @return [SSO-Server端-认证地址 ]
     */
    public String buildServerAuthorizeUrl(String clientLoginUrl, String scope, String state) {
        Oauth2ClientConfig clientConfig = getClientConfig();
        // 服务端认证地址
        String serverUrl = clientConfig.splicingAuthorizeUrl();

        // 拼接 response_type
        serverUrl = SaFoxUtil.joinParam(serverUrl, paramName.getResponseType(), "code");

        // 拼接客户端标识
        String clientId = clientConfig.getClientId();
        serverUrl = SaFoxUtil.joinParam(serverUrl, paramName.getClientId(), clientId);

        // 重定向地址
        serverUrl = SaFoxUtil.joinParam(serverUrl, paramName.getRedirectUri(), SaFoxUtil.encodeUrl(clientLoginUrl));

        // scope
        if (scope != null && !scope.isEmpty()) {
            serverUrl = SaFoxUtil.joinParam(serverUrl, paramName.getScope(), scope);
        }

        // state
        if (state != null && !state.isEmpty()) {
            serverUrl = SaFoxUtil.joinParam(serverUrl, paramName.getState(), state);
        }

        return serverUrl;
    }

    /**
     * 获取底层使用的SsoClient配置对象
     * @return /
     */
    public Oauth2ClientConfig getClientConfig() {
        return SaOauth2ClientManager.getClientConfig();
    }
}
