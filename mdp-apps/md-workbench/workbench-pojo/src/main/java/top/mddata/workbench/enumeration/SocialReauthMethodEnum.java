package top.mddata.workbench.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.exception.BizException;

import java.util.Arrays;

/**
 * 二次验证方式
 *
 * <p>记录 reauthTicket 通过哪种凭据签发，解绑等操作据此判断剩余登录能力。</p>
 *
 * @author tangyh
 */
@Getter
@AllArgsConstructor
public enum SocialReauthMethodEnum {
    /** 当前账号密码 */
    PASSWORD("password", "密码验证"),
    /** 手机短信验证码 */
    PHONE("phone", "手机验证码"),
    /** 邮箱验证码 */
    EMAIL("email", "邮箱验证码"),
    /** 已绑定第三方身份重新授权 */
    SOCIAL("social", "第三方重新授权");

    @JsonValue
    private final String code;
    private final String label;

    /**
     * 按前端编码解析验证方式
     *
     * @param code 前端编码
     * @return 验证方式枚举
     */
    @JsonCreator
    public static SocialReauthMethodEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new BizException("不支持的二次验证方式"));
    }
}
