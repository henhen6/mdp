package top.mddata.base.captcha.graphic.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import top.mddata.base.captcha.graphic.service.GraphicCaptchaService;
import top.mddata.base.constant.Constants;

/**
 * 图形验证码自动配置
 *
 * @author henhen
 */
@AutoConfiguration
@Slf4j
@EnableConfigurationProperties(GraphicCaptchaProperties.class)
@ConditionalOnProperty(prefix = GraphicCaptchaProperties.PREFIX, name = Constants.ENABLED, havingValue = "true", matchIfMissing = true)
public class GraphicCaptchaAutoConfiguration {


    /**
     * 验证码服务接口配置
     */
    @Bean
    @ConditionalOnMissingBean
    public GraphicCaptchaService graphicCaptchaService(GraphicCaptchaProperties properties) {
        log.info("[图形验证码] 加载成功");
        return new GraphicCaptchaService(properties);
    }

}
