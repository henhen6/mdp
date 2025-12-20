package top.mddata.workbench.enumeration;

import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

/**
 * <p>
 * 认证方式
 * [01-密码 02-手机短信验证码 03-邮箱验证码登录]
 * </p>
 *
 * @author henhen6
 * @date 2025年07月10日08:32:48
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "AuthTypeEnum", description = "认证方式-枚举")
public enum AuthTypeEnum implements BaseEnum<String> {
    /**
     * 用户名密码 + 验证码登录
     */
    CAPTCHA("01", "用户名密码验证码登录"),
    /**
     * 用户名密码登录
     */
    USERNAME("02", "用户名密码登录"),
    /**
     * 手机 + 短信验证码登录
     */
    PHONE("03", "手机短信验证码登录"),
    /**
     * 邮箱 + 密码登录
     */
    EMAIL("04", "邮箱密码登录");

    @Schema(description = "code")
    @EnumValue
    private String code;
    @Schema(description = "描述")
    private String desc;
}
