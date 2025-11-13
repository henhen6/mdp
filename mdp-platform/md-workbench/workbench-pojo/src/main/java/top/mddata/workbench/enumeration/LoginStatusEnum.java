package top.mddata.workbench.enumeration;

import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.stream.Stream;

/**
 * @author henhen6
 * @date 2021/11/12 9:06
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "LoginStatusEnum", description = "登录状态-枚举")
public enum LoginStatusEnum implements BaseEnum<String> {
    /**
     * 登录成功
     */
    SUCCESS("01", "登录成功"),
    /**
     * 切换组织
     */
    SWITCH("02", "切换组织"),

    /**
     * 验证码错误
     */
    CAPTCHA_ERROR("10", "验证码错误"),
    /**
     * 短信验证码错误
     */
    SMS_CODE_ERROR("11", "短信验证码错误"),
    /**
     * 邮箱验证码错误
     */
    EMAIL_CODE_ERROR("12", "邮箱验证码错误"),
    /**
     * 账号密码错误
     */
    PASSWORD_ERROR("13", "账号密码错误"),
    /**
     * 账号异常
     */
    USER_ERROR("14", "账号异常");


    @Schema(description = "code")
    @EnumValue
    private String code;
    @Schema(description = "描述")
    private String desc;

    /**
     * 根据当前枚举的name匹配
     */
    public static LoginStatusEnum match(String val, LoginStatusEnum def) {
        return Stream.of(values()).parallel().filter(item -> item.name().equalsIgnoreCase(val)).findAny().orElse(def);
    }

    public static LoginStatusEnum get(String val) {
        return match(val, null);
    }

    public boolean eq(LoginStatusEnum val) {
        return val != null && val.name().equals(this.name());
    }

}
