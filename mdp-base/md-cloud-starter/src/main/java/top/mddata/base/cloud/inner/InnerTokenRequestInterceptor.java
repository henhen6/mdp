package top.mddata.base.cloud.inner;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Feign 调用内部端点（/inner/**）时自动携带内部令牌，
 * 与服务端 InnerAuthInterceptor（md-boot）配对。
 * 对本应用发出的所有 Feign 请求生效，仅 /inner/ 路径注入令牌头。
 *
 * @author henhen
 */
public class InnerTokenRequestInterceptor implements RequestInterceptor {

    private final String innerToken;

    public InnerTokenRequestInterceptor(String innerToken) {
        this.innerToken = innerToken;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (template.url() != null && template.url().contains("/inner/")) {
            template.header("X-Inner-Token", innerToken);
        }
    }
}
