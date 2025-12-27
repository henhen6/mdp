package top.mddata.base.captcha.graphic.enumeration;

import com.wf.captcha.*;
import com.wf.captcha.base.Captcha;
import lombok.Getter;

/**
 * 图形验证码类型
 *
 * @author henhen
 */
@Getter
public enum GraphicCaptchaType {

    /**
     * 算术
     */
    ARITHMETIC(ArithmeticCaptcha.class),

    /**
     * 中文
     */
    CHINESE(ChineseCaptcha.class),

    /**
     * 中文动图
     */
    CHINESE_GIF(ChineseGifCaptcha.class),

    /**
     * 动图
     */
    GIF(GifCaptcha.class),

    /**
     * png格式
     */
    SPEC(SpecCaptcha.class);

    /**
     * 验证码实现
     */
    private final Class<? extends Captcha> captchaImpl;

    GraphicCaptchaType(Class<? extends Captcha> captchaImpl) {
        this.captchaImpl = captchaImpl;
    }

}