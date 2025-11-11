package top.mddata.common.configurer;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 基础配置
 * @author henhen6
 * @since 2025/7/8 22:26
 */
public class BasicConfigurer implements WebMvcConfigurer {
    /**
     * 拦截器需要排除拦截的地址
     */
    protected String[] getExcludeCommonPathPatterns() {
        return new String[]{
                "/*.css",
                "/*.js",
                "/*.html",
                "/error",
                "/login",
                "/v2/api-docs",
                "/v2/api-docs-ext",
                "/swagger-resources/**",
                "/webjars/**",

                "/",
                "/csrf",

                "/META-INF/resources/**",
                "/resources/**",
                "/static/**",
                "/public/**",
                "classpath:/META-INF/resources/**",
                "classpath:/resources/**",
                "classpath:/static/**",
                "classpath:/public/**",

                "/cache/**",
                "/swagger-ui.html**",
                "/doc.html**"
        };
    }
}
