package top.mddata.base.captcha.graphic.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import top.mddata.base.captcha.graphic.properties.GraphicCaptchaProperties;

/**
 * 图形验证码自动配置
 *
 * @author henhen
 */
@AutoConfiguration
@EnableConfigurationProperties(GraphicCaptchaProperties.class)
public class GraphicCaptchaPropertiesAutoConfiguration {

}
