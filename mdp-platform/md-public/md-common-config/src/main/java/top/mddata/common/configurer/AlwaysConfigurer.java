package top.mddata.common.configurer;

import top.mddata.common.interceptor.NotAllowWriteInterceptor;
import top.mddata.common.properties.SystemProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 永远执行的配置
 *
 * @author henhen6
 * @date 2018/8/25
 */
@RequiredArgsConstructor
public class AlwaysConfigurer implements WebMvcConfigurer {
    private final SystemProperties systemProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new NotAllowWriteInterceptor(systemProperties))
                .addPathPatterns("/**")
                .order(Integer.MIN_VALUE);
    }
}
