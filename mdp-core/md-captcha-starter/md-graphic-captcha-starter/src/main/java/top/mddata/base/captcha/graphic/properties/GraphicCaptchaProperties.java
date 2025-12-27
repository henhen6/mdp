
package top.mddata.base.captcha.graphic.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.mddata.base.captcha.graphic.enumeration.GraphicCaptchaType;
import top.mddata.base.constant.Constants;

import static top.mddata.base.captcha.graphic.properties.GraphicCaptchaProperties.PREFIX;

/**
 * 图形验证码配置
 *
 * @author henhen
 */
@Setter
@Getter
@ConfigurationProperties(PREFIX)
public class GraphicCaptchaProperties {
    public static final String PREFIX = Constants.PROJECT_PREFIX + ".captcha.graphic";
    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 类型
     */
    private GraphicCaptchaType type = GraphicCaptchaType.SPEC;

    /**
     * 内容长度
     */
    private int length = 4;

    /**
     * 宽度
     */
    private int width = 111;

    /**
     * 高度
     */
    private int height = 36;

    /**
     * 字体
     */
    private String fontName;

    /**
     * 字体大小
     */
    private int fontSize = 25;

}
