package top.mddata.base.exception;

import top.mddata.base.exception.code.ExceptionCode;

/**
 * 验证码校验异常
 *
 * @author henhen6
 * @version 1.0
 * @see Exception
 */
public class CaptchaException extends BaseUncheckedException {


    public CaptchaException(String format, Object... args) {
        super(ExceptionCode.CAPTCHA_ERROR.getCode(), format, args);
    }

    public static CaptchaException wrap(String format, Object... args) {
        return new CaptchaException(format, args);
    }

    @Override
    public String toString() {
        return "BizException [message=" + getMessage() + ", code=" + getCode() + "]";
    }
}
