package top.mddata.workbench.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * <p>
 * 登录类型
 * </p>
 *
 * @author henhen6
 * @date 2025年07月10日08:32:48
 */
@Getter
@Schema(title = "LoginType", description = "登录类型-枚举")
public enum LoginType {
    /**
     * 用户名密码 + 验证码登录
     */
    CAPTCHA,
    /**
     * 用户名密码登录
     */
    USERNAME,
    /**
     * 手机 + 短信验证码登录
     */
    PHONE,
    /**
     * 邮箱 + 密码登录
     */
    EMAIL;

}
