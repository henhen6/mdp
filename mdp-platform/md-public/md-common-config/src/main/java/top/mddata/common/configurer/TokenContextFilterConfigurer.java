package top.mddata.common.configurer;

import top.mddata.common.interceptor.TokenContextFilter;
import top.mddata.common.properties.IgnoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 *  请求头配置类
 *
 * @author henhen6
 * @date 2018/8/25
 */
@RequiredArgsConstructor
public class TokenContextFilterConfigurer extends BasicConfigurer {
    private final IgnoreProperties ignoreProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenContextFilter(ignoreProperties))
                .excludePathPatterns(getExcludeCommonPathPatterns())
                .addPathPatterns("/**").order(-20);
    }
}
