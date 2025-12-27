package top.mddata.base.captcha.graphic.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import com.wf.captcha.base.Captcha;
import top.mddata.base.captcha.graphic.autoconfigure.GraphicCaptchaProperties;

import java.awt.*;

/**
 * 图形验证码接口
 *
 * @author henhen
 */
public class GraphicCaptchaService {

    private final GraphicCaptchaProperties properties;

    public GraphicCaptchaService(GraphicCaptchaProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取验证码实例
     *
     * @return 验证码实例
     */
    public Captcha getCaptcha() {
        Captcha captcha = ReflectUtil.newInstance(properties.getType().getCaptchaImpl(), properties
                .getWidth(), properties.getHeight());
        captcha.setLen(properties.getLength());
        if (CharSequenceUtil.isNotBlank(properties.getFontName())) {
            captcha.setFont(new Font(properties.getFontName(), Font.PLAIN, properties.getFontSize()));
        }
        return captcha;
    }
}
