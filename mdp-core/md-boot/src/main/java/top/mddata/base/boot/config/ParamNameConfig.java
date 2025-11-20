package top.mddata.base.boot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.mddata.base.boot.handler.ParamArgumentProcessor;
import top.mddata.base.boot.handler.ParamAttrProcessor;

import java.util.List;

/**
 * 参数别名注册
 *
 * @author henhen6
 * @since 2025/9/3 11:46
 */
@Configuration
public class ParamNameConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new ParamAttrProcessor());
        argumentResolvers.add(new ParamArgumentProcessor());
    }
}
