package top.mddata.base.cloud.inner;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * 内部端点 Feign 令牌自动装配：所有引入 md-cloud-starter 的微服务，
 * 其发出的 Feign 请求命中 /inner/ 路径时自动携带 X-Inner-Token。
 *
 * @author henhen
 */
@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
public class InnerFeignAutoConfiguration {

    @Value("${mdp.inner.token:}")
    private String innerToken;

    @Bean
    public InnerTokenRequestInterceptor innerTokenRequestInterceptor() {
        return new InnerTokenRequestInterceptor(innerToken);
    }
}
