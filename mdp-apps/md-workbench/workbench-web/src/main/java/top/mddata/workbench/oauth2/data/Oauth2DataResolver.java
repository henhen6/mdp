package top.mddata.workbench.oauth2.data;

import cn.dev33.satoken.httpauth.basic.SaHttpBasicUtil;
import cn.dev33.satoken.oauth2.data.model.request.ClientIdAndSecretModel;
import cn.dev33.satoken.oauth2.error.SaOAuth2ErrorCode;
import cn.dev33.satoken.oauth2.exception.SaOAuth2Exception;
import cn.dev33.satoken.util.SaFoxUtil;
import top.mddata.base.oauth2.core.request.Oauth2ClientCredentials;

/**
 *Sa-Token OAuth2 数据解析器，负责 Web 交互层面的数据进出：
 * 1、从请求中按照指定格式读取数据
 * 2、构建数据输出格式
 * @author henhen6
 * @date 2025年11月13日10:27:53
 */
public class Oauth2DataResolver {
    /**
     * 数据读取：从请求对象中读取 ClientId、Secret，如果获取不到则抛出异常
     *
     * @param param /
     * @return /
     */
    public static ClientIdAndSecretModel readClientIdAndSecret(Oauth2ClientCredentials param) {
        // 优先从请求参数中获取
        String clientId = param.getClientId();
        String clientSecret = param.getClientSecret();
        if (SaFoxUtil.isNotEmpty(clientId)) {
            return new ClientIdAndSecretModel(clientId, clientSecret);
        }

        // 如果请求参数中没有提供 client_id 参数，则尝试从 请求头的 Authorization 中获取
        String authorizationValue = SaHttpBasicUtil.getAuthorizationValue();
        if (SaFoxUtil.isNotEmpty(authorizationValue)) {
            // 按首个冒号切分，避免 client_secret 本身含冒号时被截断
            String[] arr = authorizationValue.split(":", 2);
            clientId = arr[0];
            if (arr.length > 1) {
                clientSecret = arr[1];
            }
            return new ClientIdAndSecretModel(clientId, clientSecret);
        }

        // 如果都没有提供，则抛出异常
        throw new SaOAuth2Exception("请提供 client 信息").setCode(SaOAuth2ErrorCode.CODE_30191);
    }
}
