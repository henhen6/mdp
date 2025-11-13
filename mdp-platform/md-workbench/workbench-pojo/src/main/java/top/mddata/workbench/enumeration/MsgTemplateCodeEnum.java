package top.mddata.workbench.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.stream.Stream;

/**
 * @author henhen6
 * @version v1.0
 * @date 2022/7/28 8:09 AM
 * @create [2022/7/28 8:09 AM ] [henhen6] [初始创建]
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "MsgTemplateCodeEnum", description = "消息模板类型-枚举")
public enum MsgTemplateCodeEnum {
    // 您的验证码为：${code}，请勿将验证码泄露给他人
    PHONE_REGISTER("手机注册"),
    // 您的验证码为：${code}，请勿将验证码泄露给他人
    PHONE_LOGIN("手机登录"),
    // 您的验证码为：${code}，请勿将验证码泄露给他人
    EMAIL_REGISTER("邮箱注册"),
    EMAIL_LOGIN("邮箱登录"),
    PHONE_EDIT("修改手机号"),
    EMAIL_EDIT("修改邮箱");
    String desc;


    /**
     * 根据当前枚举的name匹配
     */
    public static MsgTemplateCodeEnum match(String val, MsgTemplateCodeEnum def) {
        return Stream.of(values()).parallel().filter(item -> item.name().equalsIgnoreCase(val)).findAny().orElse(def);
    }

    public static MsgTemplateCodeEnum get(String val) {
        return match(val, null);
    }

    public boolean eq(MsgTemplateCodeEnum val) {
        return val != null && val.name().equals(this.name());
    }

    public boolean eq(String val) {
        return this.name().equals(val);
    }

}
