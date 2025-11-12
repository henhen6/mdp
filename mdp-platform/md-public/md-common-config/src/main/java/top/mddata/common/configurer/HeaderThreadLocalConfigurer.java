package top.mddata.common.configurer;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import top.mddata.common.interceptor.HeaderThreadLocalInterceptor;

/**
 *  请求头配置类
 *
 * @author henhen6
 * @date 2018/8/25
 */
@RequiredArgsConstructor
public class HeaderThreadLocalConfigurer extends BasicConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HeaderThreadLocalInterceptor())
                .excludePathPatterns(getExcludeCommonPathPatterns())
                .addPathPatterns("/**").order(-20);
    }
}
